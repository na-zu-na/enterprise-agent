package org.cc.enterpriseagent.knowledgebase.controller;

import jakarta.validation.Valid;
import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.document.vo.DocumentUploadVO;
import org.cc.enterpriseagent.document.vo.DocumentPageVO;
import org.cc.enterpriseagent.knowledgebase.dto.CreateKnowledgeBaseRequestDTO;
import org.cc.enterpriseagent.knowledgebase.dto.UpdateKnowledgeBaseRequestDTO;
import org.cc.enterpriseagent.knowledgebase.service.KnowledgeBaseService;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseDetailVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseListVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class KnowledgeBaseController {
    @Autowired
    private KnowledgeBaseService  knowledgeBaseService;

    @PostMapping("/knowledge-bases")
    public Result<KnowledgeBaseVO> createKnowledgeBase(@RequestBody CreateKnowledgeBaseRequestDTO createKnowledgeBaseRequestDTO){
        return knowledgeBaseService.createKnowledgeBase(createKnowledgeBaseRequestDTO);
    }

    @GetMapping("/knowledge-bases")
    public Result<List<KnowledgeBaseListVO>> getAccessibleKnowledgeBases() {
        return knowledgeBaseService.getAccessibleKnowledgeBases();
    }

    @GetMapping("/knowledge-bases/{id}")
    public Result<KnowledgeBaseDetailVO> getKnowledgeBaseDetail(@PathVariable Long id) {
        return knowledgeBaseService.getKnowledgeBaseDetail(id);
    }

    @PutMapping("/knowledge-bases/{id}")
    public Result<Void> updateKnowledgeBase(@PathVariable Long id,
                                            @Valid @RequestBody UpdateKnowledgeBaseRequestDTO requestDTO) {
        return knowledgeBaseService.updateKnowledgeBase(id, requestDTO);
    }

    @DeleteMapping("/knowledge-bases/{id}")
    public Result<Void> deleteKnowledgeBase(@PathVariable Long id) {
        return knowledgeBaseService.deleteKnowledgeBase(id);
    }

    @PostMapping("/knowledge-bases/{id}/documents")
    public Result<DocumentUploadVO> uploadDocument(@PathVariable Long id,
                                                    @RequestParam("file") MultipartFile file){
        return knowledgeBaseService.uploadDocument(id,file);
    }

    @GetMapping("/knowledge-bases/{id}/documents")
    public Result<DocumentPageVO> getKnowledgeBaseDocuments(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "fileType", required = false) String fileType) {
        return knowledgeBaseService.getKnowledgeBaseDocuments(id, page, size, keyword, status, fileType);
    }
}
