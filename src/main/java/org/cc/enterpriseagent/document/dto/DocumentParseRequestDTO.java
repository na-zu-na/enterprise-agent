package org.cc.enterpriseagent.document.dto;

import lombok.Data;

@Data
public class DocumentParseRequestDTO {

    private Long documentId;
    private Long knowledgeBaseId;

    private String name;
    private String originalName;

    private String fileType;
    private Long fileSize;

    private String storagePath;

    private Integer version;
}