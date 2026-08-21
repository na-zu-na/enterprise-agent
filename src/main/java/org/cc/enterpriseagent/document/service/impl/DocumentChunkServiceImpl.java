package org.cc.enterpriseagent.document.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.document.event.DocumentChunksReplacedEvent;
import org.cc.enterpriseagent.document.vo.DocumentChunkResponseVO;
import org.cc.enterpriseagent.document.entity.DocumentChunk;
import org.cc.enterpriseagent.document.entity.KnowledgeDocument;
import org.cc.enterpriseagent.document.mapper.DocumentChunkMapper;
import org.cc.enterpriseagent.document.service.DocumentChunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentChunkServiceImpl extends ServiceImpl<DocumentChunkMapper, DocumentChunk> implements DocumentChunkService{
    @Autowired
    private DocumentChunkMapper documentChunkMapper;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public boolean replaceChunks(KnowledgeDocument knowledgeDocument, List<DocumentChunkResponseVO> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalStateException("AI未提供有效模块");
        }

        documentChunkMapper.deleteByDocumentId(knowledgeDocument.getId());

        List<DocumentChunk> list = chunks.stream().map(chunk -> toEntity(knowledgeDocument, chunk)).toList();
        boolean saved = saveBatch(list, 500);
        if (!saved) {
            throw new IllegalStateException("Chunk 保存失败");
        }

        // 只有当前数据库事务提交成功后，监听器才会真正同步 ES。
        applicationEventPublisher.publishEvent(new DocumentChunksReplacedEvent(knowledgeDocument.getId()));

        return true;
    }

    private DocumentChunk toEntity(
            KnowledgeDocument document,
            DocumentChunkResponseVO source
    ) {
        DocumentChunk target = new DocumentChunk();

        target.setDocumentId(document.getId());
        target.setKnowledgeBaseId(document.getKnowledgeBaseId());
        target.setDocumentVersion(document.getVersion());

        target.setChunkIndex(source.getChunkIndex());
        target.setContent(source.getContent());
        target.setCharCount(source.getCharCount());

        if(source.getEmbedding()==null ||  source.getEmbedding().size()!=1024){
            throw new IllegalStateException("Chunk embedding 必须为 1024 维");
        }
        target.setEmbedding(source.getEmbedding());

        var meta = source.getMetadata();
        target.setSectionTitle(meta == null ? null : meta.getSectionTitle());
        target.setSectionLevel(meta == null ? null : meta.getSectionLevel());

        Map<String, Object> metadata = new HashMap<>();
        if (meta != null) {
            metadata.put("fileType", meta.getFileType());
            metadata.put("documentName", meta.getDocumentName());
            metadata.put("pageNumber", meta.getPageNumber());
            metadata.put("version", meta.getVersion());
        }
        target.setMetadata(metadata);

        return target;
    }
}
