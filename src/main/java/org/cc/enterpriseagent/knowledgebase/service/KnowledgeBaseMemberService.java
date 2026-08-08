package org.cc.enterpriseagent.knowledgebase.service;

import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.knowledgebase.dto.AddKnowledgeBaseMemberRequestDTO;
import org.cc.enterpriseagent.knowledgebase.dto.UpdateKnowledgeBaseMemberRoleRequestDTO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseMemberVO;

import java.util.List;

public interface KnowledgeBaseMemberService {
    Result<List<KnowledgeBaseMemberVO>> getKnowledgeBaseMembers(Long knowledgeBaseId);

    Result<Void> addKnowledgeBaseMember(Long knowledgeBaseId, AddKnowledgeBaseMemberRequestDTO requestDTO);

    Result<Void> updateKnowledgeBaseMemberRole(Long knowledgeBaseId, Long userId,
                                               UpdateKnowledgeBaseMemberRoleRequestDTO requestDTO);

    Result<Void> removeKnowledgeBaseMember(Long knowledgeBaseId, Long userId);
}
