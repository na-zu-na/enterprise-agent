package org.cc.enterpriseagent.document.controller;

import jakarta.validation.Valid;
import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.document.dto.DocumentParseResponseVO;
import org.cc.enterpriseagent.document.dto.UpdateDocumentNameRequestDTO;
import org.cc.enterpriseagent.document.service.DocumentService;
import org.cc.enterpriseagent.document.vo.DocumentDetailVO;
import org.cc.enterpriseagent.document.vo.DocumentPreviewVO;
import org.cc.enterpriseagent.knowledgebase.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final KnowledgeBaseService knowledgeBaseService;

    private final DocumentService documentService;

    public DocumentController(KnowledgeBaseService knowledgeBaseService, DocumentService documentService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.documentService = documentService;
    }

    @GetMapping("/{id}")
    public Result<DocumentDetailVO> getDocumentDetail(@PathVariable Long id) {
        return knowledgeBaseService.getDocumentDetail(id);
    }

    @PutMapping("/{id}")
    public Result<Void> updateDocumentName(@PathVariable Long id,
                                           @Valid @RequestBody UpdateDocumentNameRequestDTO requestDTO) {
        return knowledgeBaseService.updateDocumentName(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        return knowledgeBaseService.deleteDocument(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        return knowledgeBaseService.downloadDocument(id);
    }

    @GetMapping("/{id}/preview")
    public Result<DocumentPreviewVO> previewDocument(@PathVariable Long id) {
        return knowledgeBaseService.previewDocument(id);
    }

    @PostMapping("/{id}/parse")
    public Result<DocumentParseResponseVO> parseDocument(@PathVariable Long id){
        return documentService.parseDocument(id);
    }
}
