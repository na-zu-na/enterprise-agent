package org.cc.enterpriseagent.agent.service;

import org.cc.enterpriseagent.agent.dto.ChatRequestDTO;
import org.cc.enterpriseagent.agent.vo.AgentMessageVO;
import org.cc.enterpriseagent.agent.vo.ConversationVO;
import org.cc.enterpriseagent.agent.vo.AgentResponseVO;
import org.cc.enterpriseagent.common.utils.Result;

import java.util.List;

public interface AgentService {
    Result<AgentResponseVO> agentChat(ChatRequestDTO requestDTO);

    Result<List<ConversationVO>> getMyConversations();

    Result<List<AgentMessageVO>> getConversationMessages(Long conversationId);
}
