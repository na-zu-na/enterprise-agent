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

    private Double distance;
}