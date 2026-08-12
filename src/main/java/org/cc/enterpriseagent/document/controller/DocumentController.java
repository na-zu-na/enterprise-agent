package org.cc.enterpriseagent.document.controller;

import jakarta.validation.Valid;
import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.document.dto.UpdateDocumentNameRequestDTO;
import org.cc.enterpriseagent.document.vo.DocumentDetailVO;
import org.cc.enterpriseagent.knowledgebase.service.KnowledgeBaseService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final KnowledgeBaseService knowledgeBaseService;

    public DocumentController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
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
}
