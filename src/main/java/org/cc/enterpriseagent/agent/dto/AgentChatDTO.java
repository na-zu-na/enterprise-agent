package org.cc.enterpriseagent.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AgentChatDTO {
    String message;

    @JsonProperty("knowledge_base_ids")
    List<Long> knowledgeBaseIds;
    @JsonProperty("conversation_id")
    Long conversationId;
}
