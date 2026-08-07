package org.cc.enterpriseagent.knowledgebase.controller;

import jakarta.validation.Valid;
import org.cc.enterpriseagent.common.Result;
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
import org.springframework.web.bind.annotation.RestController;

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
}
