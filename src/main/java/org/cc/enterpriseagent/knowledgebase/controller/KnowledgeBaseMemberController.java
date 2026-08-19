package org.cc.enterpriseagent.knowledgebase.controller;

import jakarta.validation.Valid;
import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.knowledgebase.dto.AddKnowledgeBaseMemberRequestDTO;
import org.cc.enterpriseagent.knowledgebase.dto.UpdateKnowledgeBaseMemberRoleRequestDTO;
import org.cc.enterpriseagent.knowledgebase.service.KnowledgeBaseMemberService;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseMemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class KnowledgeBaseMemberController {
    @Autowired
    private KnowledgeBaseMemberService knowledgeBaseMemberService;

    @GetMapping("/knowledge-bases/{id}/members")
    public Result<List<KnowledgeBaseMemberVO>> getKnowledgeBaseMembers(@PathVariable Long id) {
        return knowledgeBaseMemberService.getKnowledgeBaseMembers(id);
    }

    @PostMapping("/knowledge-bases/{id}/members")
    public Result<Void> addKnowledgeBaseMember(@PathVariable Long id,
                                               @Valid @RequestBody AddKnowledgeBaseMemberRequestDTO requestDTO) {
        return knowledgeBaseMemberService.addKnowledgeBaseMember(id, requestDTO);
    }

    @PutMapping("/knowledge-bases/{id}/members/{userId}")
    public Result<Void> updateKnowledgeBaseMemberRole(@PathVariable Long id, @PathVariable Long userId,
                                                      @Valid @RequestBody UpdateKnowledgeBaseMemberRoleRequestDTO requestDTO) {
        return knowledgeBaseMemberService.updateKnowledgeBaseMemberRole(id, userId, requestDTO);
    }

    @DeleteMapping("/knowledge-bases/{id}/members/{userId}")
    public Result<Void> removeKnowledgeBaseMember(@PathVariable Long id, @PathVariable Long userId) {
        return knowledgeBaseMemberService.removeKnowledgeBaseMember(id, userId);
    }
}
