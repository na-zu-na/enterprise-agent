package org.cc.enterpriseagent.document.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.document.vo.DocumentChunkResponseVO;
import org.cc.enterpriseagent.document.entity.DocumentChunk;
import org.cc.enterpriseagent.document.entity.KnowledgeDocument;
import org.cc.enterpriseagent.document.mapper.DocumentChunkMapper;
import org.cc.enterpriseagent.document.service.DocumentChunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentChunkServiceImpl extends ServiceImpl<DocumentChunkMapper, DocumentChunk> implements DocumentChunkService{
    @Autowired
    private DocumentChunkMapper documentChunkMapper;

    @Override
    @Transactional
    public boolean replaceChunks(KnowledgeDocument knowledgeDocument, List<DocumentChunkResponseVO> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalStateException("AI未提供有效模块");
        }

        documentChunkMapper.deleteByDocumentId(knowledgeDocument.getId());

        List<DocumentChunk> list = chunks.stream().map(chunk -> toEntity(knowledgeDocument, chunk)).toList();
        return saveBatch(list, 500);
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
