package org.cc.enterpriseagent.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.agent.dto.AgentApprovalResponseDTO;
import org.cc.enterpriseagent.agent.dto.AgentChatDTO;
import org.cc.enterpriseagent.agent.dto.ApprovalResponseRequestDTO;
import org.cc.enterpriseagent.agent.dto.ChatRequestDTO;
import org.cc.enterpriseagent.agent.entity.AgentConversation;
import org.cc.enterpriseagent.agent.entity.AgentMessage;
import org.cc.enterpriseagent.agent.mapper.AgentMapper;
import org.cc.enterpriseagent.agent.mapper.AgentMessageMapper;
import org.cc.enterpriseagent.agent.service.AgentService;
import org.cc.enterpriseagent.agent.vo.AgentMessageVO;
import org.cc.enterpriseagent.agent.vo.AgentResponseVO;
import org.cc.enterpriseagent.agent.vo.ApprovalVO;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, AgentConversation> implements AgentService {

    private static final String USER_ROLE = "USER";
    private static final String ASSISTANT_ROLE = "ASSISTANT";
    private static final String APPROVAL_REQUIRED_STATUS = "approval_required";
    private static final String COMPLETED_STATUS = "completed";

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
        if (!saveMessage(conversationId, USER_ROLE, requestDTO.getQuery(), null,
                null, null, null)) {
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
        if (!saveMessage(conversationId, ASSISTANT_ROLE, response.getAnswer(), response.getStatus(),
                response.getApproval(), wrapCitations(response), response.getCheckpoint())) {
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

    @Override
    @Transactional
    public Result<Void> deleteConversation(Long conversationId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        int deleted = agentMapper.delete(new LambdaQueryWrapper<AgentConversation>()
                .eq(AgentConversation::getId, conversationId)
                .eq(AgentConversation::getUserId, userId));
        if (deleted == 0) {
            return Result.error(404, "会话不存在或无访问权限");
        }
        return Result.success();
    }

    @Override
    @Transactional
    public Result<AgentResponseVO> respondApproval(ApprovalResponseRequestDTO requestDTO) {
        String token = UserContext.getToken();
        Long userId = UserContext.getUserId();
        if (userId == null || token == null) {
            return Result.error(401, "用户未登录");
        }
        AgentConversation conversation = findOwnedConversation(requestDTO.getConversationId(), userId);
        if (conversation == null) {
            return Result.error(404, "会话不存在或无访问权限");
        }

        AgentMessage pendingApprovalMessage = null;
        if (Boolean.TRUE.equals(requestDTO.getApproved())) {
            pendingApprovalMessage = findPendingApprovalMessage(conversation.getId(), requestDTO.getInterruptId());
            if (pendingApprovalMessage == null) {
                return Result.error(404, "待审批消息不存在或已处理");
            }
        }

        AgentApprovalResponseDTO agentApprovalResponseDTO = new AgentApprovalResponseDTO();
        agentApprovalResponseDTO.setConversationId(requestDTO.getConversationId());
        agentApprovalResponseDTO.setApproved(requestDTO.getApproved());
        agentApprovalResponseDTO.setInterruptId(requestDTO.getInterruptId());

        try {
            Result<AgentResponseVO> response=restClient.post()
                    .uri("/agent/approvals/respond")
                    .header("Authorization", "Bearer " + token)
                    .body(agentApprovalResponseDTO)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Result<AgentResponseVO>>() {});
            if (response == null || response.getCode() == null || response.getData() == null) {
                return Result.error(500,"AI服务调用失败");
            }

            if (response.getCode() != 200) {
                return Result.error(response.getCode(),response.getMessage());
            }

            AgentResponseVO agentResponse = response.getData();
            if (pendingApprovalMessage != null && agentMessageMapper.update(null,
                    new LambdaUpdateWrapper<AgentMessage>()
                            .eq(AgentMessage::getId, pendingApprovalMessage.getId())
                            .eq(AgentMessage::getStatus, APPROVAL_REQUIRED_STATUS)
                            .set(AgentMessage::getStatus, COMPLETED_STATUS)) <= 0) {
                markTransactionRollbackOnly();
                return Result.error(409, "Approval has already been processed");
            }

            if (!saveMessage(conversation.getId(), ASSISTANT_ROLE, agentResponse.getAnswer(),
                    agentResponse.getStatus(), agentResponse.getApproval(),
                    wrapCitations(agentResponse), agentResponse.getCheckpoint())) {
                markTransactionRollbackOnly();
                return Result.error(500, "AI message save failed");
            }

            if (agentResponse.getTitle() != null && !agentResponse.getTitle().isBlank()) {
                conversation.setTitle(agentResponse.getTitle());
            }
            if (agentMapper.updateById(conversation) <= 0) {
                markTransactionRollbackOnly();
                return Result.error(500, "Conversation update failed");
            }

            agentResponse.setConversationId(conversation.getId());
            return Result.success(agentResponse);
        } catch (Exception e) {
            markTransactionRollbackOnly();
            return Result.error(502, "AI service call failed");
        }
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

    private AgentMessage findPendingApprovalMessage(Long conversationId, String interruptId) {
        return agentMessageMapper.selectList(new LambdaQueryWrapper<AgentMessage>()
                        .eq(AgentMessage::getConversationId, conversationId)
                        .eq(AgentMessage::getRole, ASSISTANT_ROLE)
                        .eq(AgentMessage::getStatus, APPROVAL_REQUIRED_STATUS))
                .stream()
                .filter(message -> message.getApproval() != null
                        && interruptId.equals(message.getApproval().get("interrupt_id")))
                .findFirst()
                .orElse(null);
    }

    private boolean saveMessage(Long conversationId, String role, String content, String status,
                                ApprovalVO approvalVO,
                                Map<String, Object> citations, Map<String, Object> checkpoint) {
        if (content == null || content.isBlank()) {
            if (approvalVO == null) {
                return false;
            } else {
                content = "操作等待审批：" + approvalVO.getAction();
            }
        }
        AgentMessage message = new AgentMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setStatus(status);
        message.setApproval(toApprovalMap(approvalVO));
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
        vo.setStatus(message.getStatus());
        vo.setApproval(toApprovalVO(message.getApproval()));
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }

    private Map<String, Object> toApprovalMap(ApprovalVO approval) {
        if (approval == null) {
            return null;
        }
        Map<String, Object> approvalMap = new HashMap<>();
        approvalMap.put("interrupt_id", approval.getInterruptId());
        approvalMap.put("action", approval.getAction());
        approvalMap.put("payload", approval.getPayload());
        return approvalMap;
    }

    @SuppressWarnings("unchecked")
    private ApprovalVO toApprovalVO(Map<String, Object> approvalMap) {
        if (approvalMap == null) {
            return null;
        }
        ApprovalVO approval = new ApprovalVO();
        approval.setInterruptId((String) approvalMap.get("interrupt_id"));
        approval.setAction((String) approvalMap.get("action"));
        Object payload = approvalMap.get("payload");
        if (payload instanceof Map<?, ?>) {
            approval.setPayload((Map<String, Object>) payload);
        }
        return approval;
    }

    private void markTransactionRollbackOnly() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
}
