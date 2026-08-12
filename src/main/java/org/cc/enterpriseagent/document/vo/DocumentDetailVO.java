package org.cc.enterpriseagent.document.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentDetailVO {
    private Long id;
    private Long knowledgeBaseId;
    private String knowledgeBaseName;
    private String name;
    private String originalName;
    private String fileType;
    private Long fileSize;
    private String status;
    private Integer version;
    private Long uploadedBy;
    private String uploaderName;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
