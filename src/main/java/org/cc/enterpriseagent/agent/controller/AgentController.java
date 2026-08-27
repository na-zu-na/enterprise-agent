package org.cc.enterpriseagent.agent.controller;

import jakarta.validation.Valid;
import org.cc.enterpriseagent.agent.dto.ChatRequestDTO;
import org.cc.enterpriseagent.agent.service.AgentService;
import org.cc.enterpriseagent.agent.vo.AgentMessageVO;
import org.cc.enterpriseagent.agent.vo.AgentResponseVO;
import org.cc.enterpriseagent.agent.vo.ConversationVO;
import org.cc.enterpriseagent.common.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AgentController {
    @Autowired
    public AgentService agentService;

    @PostMapping("/chat")
    public Result<AgentResponseVO> agentChatController(@Valid @RequestBody ChatRequestDTO requestDTO){
        return agentService.agentChat(requestDTO);
    }

    @GetMapping("/agent/conversations")
    public Result<List<ConversationVO>> getMyConversations() {
        return agentService.getMyConversations();
    }

    @GetMapping("/agent/conversations/{conversationId}/messages")
    public Result<List<AgentMessageVO>> getConversationMessages(@PathVariable Long conversationId) {
        return agentService.getConversationMessages(conversationId);
    }
}
