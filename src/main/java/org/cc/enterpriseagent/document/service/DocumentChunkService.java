package org.cc.enterpriseagent.document.service;

import org.cc.enterpriseagent.document.vo.DocumentChunkResponseVO;
import org.cc.enterpriseagent.document.entity.KnowledgeDocument;

import java.util.List;

public interface DocumentChunkService {
    void replaceChunks(KnowledgeDocument knowledgeDocument,List<DocumentChunkResponseVO> data);
}
