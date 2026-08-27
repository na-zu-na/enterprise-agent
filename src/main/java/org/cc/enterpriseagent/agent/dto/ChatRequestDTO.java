package org.cc.enterpriseagent.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ChatRequestDTO {

    @NotBlank(message = "问题不能为空")
    private String query;

    private List<Long> knowledgeBaseIds;

    private Long conversationId;
}