package org.cc.enterpriseagent.agent.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AgentMessageVO {
    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private Map<String, Object> citations;
    private Map<String, Object> checkpoint;
    private LocalDateTime createdAt;
}
