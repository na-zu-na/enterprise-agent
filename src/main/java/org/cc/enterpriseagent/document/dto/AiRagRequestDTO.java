package org.cc.enterpriseagent.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AiRagRequestDTO {

    private String query;

    @JsonProperty("knowledge_base_ids")
    private List<Long> knowledgeBaseIds;

    @JsonProperty("top_k")
    private Integer topK;

    @JsonProperty("candidate_k")
    private Integer candidateK;

    @JsonProperty("rrf_top_k")
    private Integer rrfTopK;
}