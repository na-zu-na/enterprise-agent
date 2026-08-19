package org.cc.enterpriseagent.document.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChunkEmbeddingResponseVO {

    @JsonProperty("chunk_id")
    private Long chunkId;

    private List<Float> embedding;
}