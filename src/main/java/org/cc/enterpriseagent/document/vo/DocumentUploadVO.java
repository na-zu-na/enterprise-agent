package org.cc.enterpriseagent.document.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentUploadVO {
    private Long id;
    private Long knowledgeBaseId;
    private String name;
    private String originalName;
    private String fileType;
    private Long fileSize;
    private String status;
    private Integer version;
    private Long uploadedBy;
    private String uploaderName;
    private LocalDateTime createdAt;
}
