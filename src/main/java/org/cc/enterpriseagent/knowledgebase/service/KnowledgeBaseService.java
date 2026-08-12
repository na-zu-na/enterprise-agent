package org.cc.enterpriseagent.knowledgebase.service;

import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.document.vo.DocumentUploadVO;
import org.cc.enterpriseagent.document.vo.DocumentPageVO;
import org.cc.enterpriseagent.document.vo.DocumentDetailVO;
import org.cc.enterpriseagent.document.dto.UpdateDocumentNameRequestDTO;
import org.cc.enterpriseagent.knowledgebase.dto.CreateKnowledgeBaseRequestDTO;
import org.cc.enterpriseagent.knowledgebase.dto.UpdateKnowledgeBaseRequestDTO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseDetailVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseListVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseVO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeBaseService {
    Result<KnowledgeBaseVO> createKnowledgeBase(CreateKnowledgeBaseRequestDTO requestDTO);

    Result<List<KnowledgeBaseListVO>> getAccessibleKnowledgeBases();

    Result<KnowledgeBaseDetailVO> getKnowledgeBaseDetail(Long knowledgeBaseId);

    Result<Void> updateKnowledgeBase(Long knowledgeBaseId, UpdateKnowledgeBaseRequestDTO requestDTO);

    Result<Void> deleteKnowledgeBase(Long knowledgeBaseId);

    Result<DocumentUploadVO> uploadDocument(Long id, MultipartFile file);

    Result<DocumentPageVO> getKnowledgeBaseDocuments(Long id, Integer page, Integer size,
                                                      String keyword, String status, String fileType);

    Result<DocumentDetailVO> getDocumentDetail(Long documentId);

    Result<Void> updateDocumentName(Long documentId, UpdateDocumentNameRequestDTO requestDTO);

    Result<Void> deleteDocument(Long documentId);

    ResponseEntity<Resource> downloadDocument(Long id);
}
