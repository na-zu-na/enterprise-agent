package org.cc.enterpriseagent.document.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CitationVO {

    private Long id;

    @JsonProperty("document_id")
    private Long documentId;

    @JsonProperty("knowledge_base_id")
    private Long knowledgeBaseId;

    @JsonProperty("chunk_index")
    private Integer chunkIndex;

    private String content;

    @JsonProperty("document_name")
    private String documentName;

    @JsonProperty("section_title")
    private String sectionTitle;

    @JsonProperty("rrf_score")
    private Double rrfScore;

    @JsonProperty("dense_rank")
    private Integer denseRank;

    @JsonProperty("dense_distance")
    private Double denseDistance;

    @JsonProperty("bm25_rank")
    private Integer bm25Rank;

    @JsonProperty("bm25_score")
    private Double bm25Score;

    @JsonProperty("rerank_score")
    private Double rerankScore;

    @JsonProperty("rerank_rank")
    private Integer rerankRank;
}