package org.cc.enterpriseagent.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AgentApprovalResponseDTO {

    @JsonProperty("conversation_id")
    private Long conversationId;

    @JsonProperty("interrupt_id")
    private String interruptId;

    private Boolean approved;
}