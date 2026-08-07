package org.cc.enterpriseagent.knowledgebase.service;

import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.knowledgebase.dto.CreateKnowledgeBaseRequestDTO;
import org.cc.enterpriseagent.knowledgebase.dto.UpdateKnowledgeBaseRequestDTO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseDetailVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseListVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseVO;

import java.util.List;

public interface KnowledgeBaseService {
    Result<KnowledgeBaseVO> createKnowledgeBase(CreateKnowledgeBaseRequestDTO requestDTO);

    Result<List<KnowledgeBaseListVO>> getAccessibleKnowledgeBases();

    Result<KnowledgeBaseDetailVO> getKnowledgeBaseDetail(Long knowledgeBaseId);

    Result<Void> updateKnowledgeBase(Long knowledgeBaseId, UpdateKnowledgeBaseRequestDTO requestDTO);

    Result<Void> deleteKnowledgeBase(Long knowledgeBaseId);
}
