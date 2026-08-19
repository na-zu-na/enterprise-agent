package org.cc.enterpriseagent.document.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DocumentChunkResponseVO {

    private Long documentId;

    private Long knowledgeBaseId;

    private Integer chunkIndex;

    private String content;

    private Integer charCount;

    private DocumentChunkMetadataVO metadata;

    private List<Float> embedding;

    @Data
    public static class DocumentChunkMetadataVO {

        @JsonProperty("file_type")
        private String fileType;

        @JsonProperty("document_name")
        private String documentName;

        private Integer version;

        @JsonProperty("section_title")
        private String sectionTitle;

        @JsonProperty("section_level")
        private Integer sectionLevel;

        @JsonProperty("page_number")
        private Integer pageNumber;
    }
}