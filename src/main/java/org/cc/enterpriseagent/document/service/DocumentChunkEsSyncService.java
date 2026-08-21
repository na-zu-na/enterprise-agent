package org.cc.enterpriseagent.document.service;

import org.cc.enterpriseagent.document.entity.DocumentChunk;

public interface DocumentChunkEsSyncService {
    public void syncDocument(Long documentId);

    public void deleteByDocumentId(Long documentId);
}
