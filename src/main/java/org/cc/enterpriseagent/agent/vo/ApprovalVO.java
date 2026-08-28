package org.cc.enterpriseagent.agent.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
    public class ApprovalVO {
        @JsonProperty("interrupt_id")
        private String interruptId;

        private String action;

        private Map<String, Object> payload;
    }