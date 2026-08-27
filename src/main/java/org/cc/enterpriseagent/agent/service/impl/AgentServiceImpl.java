package org.cc.enterpriseagent.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.agent.dto.AgentChatDTO;
import org.cc.enterpriseagent.agent.dto.ChatRequestDTO;
import org.cc.enterpriseagent.agent.entity.AgentConversation;
import org.cc.enterpriseagent.agent.entity.AgentMessage;
import org.cc.enterpriseagent.agent.mapper.AgentMapper;
import org.cc.enterpriseagent.agent.mapper.AgentMessageMapper;
import org.cc.enterpriseagent.agent.service.AgentService;
import org.cc.enterpriseagent.agent.vo.AgentMessageVO;
import org.cc.enterpriseagent.agent.vo.AgentResponseVO;
import org.cc.enterpriseagent.agent.vo.ConversationVO;
import org.cc.enterpriseagent.common.UserContext;
import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.knowledgebase.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, AgentConversation> implements AgentService {

    private static final String USER_ROLE = "USER";
    private static final String ASSISTANT_ROLE = "ASSISTANT";

    @Autowired
    private RestClient restClient;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private AgentMessageMapper agentMessageMapper;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Override
    @Transactional
    public Result<AgentResponseVO> agentChat(ChatRequestDTO requestDTO) {
        String token = UserContext.getToken();
        Long userId = UserContext.getUserId();
        if (userId == null || token == null) {
            return Result.error(401, "用户未登录");
        }
        if (requestDTO == null || requestDTO.getQuery() == null || requestDTO.getQuery().isBlank()) {
            return Result.error(400, "问题不能为空");
        }

        List<Long> effectiveKnowledgeBaseIds = resolveKnowledgeBaseIds(userId, requestDTO.getKnowledgeBaseIds());
        if (effectiveKnowledgeBaseIds == null) {
            return Result.error(403, "无访问知识库权限");
        }
        if (effectiveKnowledgeBaseIds.isEmpty()) {
            return Result.error(400, "没有可查询的知识库");
        }

        AgentConversation conversation;
        if (requestDTO.getConversationId() == null) {
            conversation = new AgentConversation();
            conversation.setUserId(userId);
            if (agentMapper.insert(conversation) <= 0) {
                return Result.error(500, "会话创建失败");
            }
        } else {
            conversation = findOwnedConversation(requestDTO.getConversationId(), userId);
            if (conversation == null) {
                return Result.error(404, "会话不存在或无访问权限");
            }
        }

        Long conversationId = conversation.getId();
        if (!saveMessage(conversationId, USER_ROLE, requestDTO.getQuery(), null, null)) {
            markTransactionRollbackOnly();
            return Result.error(500, "用户消息保存失败");
        }

        AgentChatDTO chatDTO = new AgentChatDTO();
        chatDTO.setKnowledgeBaseIds(effectiveKnowledgeBaseIds);
        chatDTO.setMessage(requestDTO.getQuery());
        chatDTO.setConversationId(conversationId);

        Result<AgentResponseVO> aiResponse;
        try {
            aiResponse = restClient.post()
                    .uri("/agent/chat")
                    .header("Authorization", "Bearer " + token)
                    .body(chatDTO)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Result<AgentResponseVO>>() {});
        } catch (Exception e) {
            markTransactionRollbackOnly();
            return Result.error(502, "AI 服务调用失败");
        }

        if (aiResponse == null || aiResponse.getCode() == null || aiResponse.getCode() != 200
                || aiResponse.getData() == null) {
            markTransactionRollbackOnly();
            return Result.error(502, "AI 服务响应异常");
        }

        AgentResponseVO response = aiResponse.getData();
        if (!saveMessage(conversationId, ASSISTANT_ROLE, response.getAnswer(),
                wrapCitations(response), response.getCheckpoint())) {
            markTransactionRollbackOnly();
            return Result.error(500, "AI 消息保存失败");
        }

        if (response.getTitle() != null && !response.getTitle().isBlank()) {
            conversation.setTitle(response.getTitle());
        }
        if (agentMapper.updateById(conversation) <= 0) {
            markTransactionRollbackOnly();
            return Result.error(500, "会话更新失败");
        }

        response.setConversationId(conversationId);
        return Result.success(response);
    }

    @Override
    public Result<List<ConversationVO>> getMyConversations() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        List<ConversationVO> conversations = agentMapper.selectList(
                        new LambdaQueryWrapper<AgentConversation>()
                                .eq(AgentConversation::getUserId, userId)
                                .orderByDesc(AgentConversation::getUpdatedAt))
                .stream()
                .map(this::toConversationVO)
                .toList();
        return Result.success(conversations);
    }

    @Override
    public Result<List<AgentMessageVO>> getConversationMessages(Long conversationId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        if (findOwnedConversation(conversationId, userId) == null) {
            return Result.error(404, "会话不存在或无访问权限");
        }

        List<AgentMessageVO> messages = agentMessageMapper.selectList(
                        new LambdaQueryWrapper<AgentMessage>()
                                .eq(AgentMessage::getConversationId, conversationId)
                                .orderByAsc(AgentMessage::getCreatedAt))
                .stream()
                .map(this::toAgentMessageVO)
                .toList();
        return Result.success(messages);
    }

    private List<Long> resolveKnowledgeBaseIds(Long userId, List<Long> requestedKnowledgeBaseIds) {
        List<Long> accessibleKnowledgeBaseIds = knowledgeBaseService.getAccessibleKnowledgeBaseIds(userId);
        if (requestedKnowledgeBaseIds == null || requestedKnowledgeBaseIds.isEmpty()) {
            return accessibleKnowledgeBaseIds;
        }
        boolean hasUnauthorizedKnowledgeBase = requestedKnowledgeBaseIds.stream()
                .anyMatch(id -> id == null || !accessibleKnowledgeBaseIds.contains(id));
        return hasUnauthorizedKnowledgeBase ? null : requestedKnowledgeBaseIds;
    }

    private AgentConversation findOwnedConversation(Long conversationId, Long userId) {
        if (conversationId == null) {
            return null;
        }
        return agentMapper.selectOne(new LambdaQueryWrapper<AgentConversation>()
                .eq(AgentConversation::getId, conversationId)
                .eq(AgentConversation::getUserId, userId));
    }

    private boolean saveMessage(Long conversationId, String role, String content,
                                Map<String, Object> citations, Map<String, Object> checkpoint) {
        if (content == null || content.isBlank()) {
            return false;
        }
        AgentMessage message = new AgentMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setCitations(citations);
        message.setCheckpoint(checkpoint);
        return agentMessageMapper.insert(message) > 0;
    }

    private Map<String, Object> wrapCitations(AgentResponseVO response) {
        return response.getCitations() == null ? null : Map.of("items", response.getCitations());
    }

    private ConversationVO toConversationVO(AgentConversation conversation) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conversation.getId());
        vo.setTitle(conversation.getTitle());
        vo.setCreatedAt(conversation.getCreatedAt());
        vo.setUpdatedAt(conversation.getUpdatedAt());
        return vo;
    }

    private AgentMessageVO toAgentMessageVO(AgentMessage message) {
        AgentMessageVO vo = new AgentMessageVO();
        vo.setId(message.getId());
        vo.setConversationId(message.getConversationId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setCitations(message.getCitations());
        vo.setCheckpoint(message.getCheckpoint());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }

    private void markTransactionRollbackOnly() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
}
