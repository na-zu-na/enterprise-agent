package org.cc.enterpriseagent.document.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cc.enterpriseagent.document.entity.DocumentChunk;
import org.cc.enterpriseagent.document.entity.KnowledgeDocument;
import org.cc.enterpriseagent.document.mapper.DocumentChunkMapper;
import org.cc.enterpriseagent.document.mapper.DocumentMapper;
import org.cc.enterpriseagent.document.service.DocumentChunkEsSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DocumentChunkEsSyncServiceImpl implements DocumentChunkEsSyncService {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentChunkMapper documentChunkMapper;

    @Value("${app.elasticsearch.document-chunk-index}")
    private String indexName;

    @Override
    public void syncDocument(Long documentId) {
        KnowledgeDocument knowledgeDocument = documentMapper.selectById(documentId);
        if (knowledgeDocument == null || Boolean.TRUE.equals(knowledgeDocument.getDeleted())) {
            throw new IllegalStateException("AI 未提供有效分块");
        }

        List<DocumentChunk> documentChunks = documentChunkMapper.selectList(
                new LambdaQueryWrapper<DocumentChunk>()
                        .eq(DocumentChunk::getDocumentId, documentId)
                        .orderByAsc(DocumentChunk::getChunkIndex)
        );

        // 重解析时先删除旧分块，避免旧版本残留在 ES。
        deleteByDocumentId(documentId);

        if (documentChunks.isEmpty()) {
            return;
        }

        try {
            BulkResponse response=elasticsearchClient.bulk(request->{
                for(DocumentChunk documentChunk : documentChunks) {
                    request.operations(operation->operation.index(
                            index->index.index(indexName)
                                    .id(String.valueOf(documentChunk.getId()))
                                    .document(toEsDocument(documentChunk, knowledgeDocument))
                    ));
                }
                return request;
            });

            if (response.errors()) {
                String errors = response.items().stream()
                        .filter(item -> item.error() != null)
                        .map(item -> item.error().reason())
                        .reduce((first, second) -> first + "; " + second)
                        .orElse("未知 ES 批量写入错误");

                throw new IllegalStateException("ES 分块同步失败: " + errors);
            }

            log.info("文档分块同步 ES 成功，documentId={}, chunkCount={}",
                    documentId, documentChunks.size());

        } catch (Exception e) {
            throw new IllegalStateException(
                    "调用 Elasticsearch 同步分块失败，documentId=" + documentId, e
            );
        }

    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        try {
            elasticsearchClient.deleteByQuery(request->request
                    .index(indexName)
                    .query(
                            query->query.term(term->term
                                    .field("document_id")
                                    .value(documentId)
                            )
                    )
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "删除 Elasticsearch 旧分块失败，documentId=" + documentId, e
            );
        }
    }

    private Map<String, Object> toEsDocument(DocumentChunk chunk,
                                             KnowledgeDocument document) {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("id", chunk.getId());
        source.put("document_id", chunk.getDocumentId());
        source.put("knowledge_base_id", chunk.getKnowledgeBaseId());
        source.put("chunk_index", chunk.getChunkIndex());
        source.put("content", chunk.getContent());
        source.put("document_name", document.getName());
        source.put("section_title", chunk.getSectionTitle());
        source.put("document_version", chunk.getDocumentVersion());
        source.put("deleted", Boolean.TRUE.equals(chunk.getDeleted()));
        return source;
    }
}
