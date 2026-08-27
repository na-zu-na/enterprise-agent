package org.cc.enterpriseagent.agent.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
