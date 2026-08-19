package org.cc.enterpriseagent.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChunkEmbeddingRequestDTO {
    @JsonProperty("chunk_id")
    private Long chunkId;

    private String content;
}