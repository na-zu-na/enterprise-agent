package org.cc.enterpriseagent.document.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmbeddingRequestDTO {

    private List<ChunkEmbeddingRequestDTO> chunks;
}