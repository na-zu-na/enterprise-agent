package org.cc.enterpriseagent.knowledgebase.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeBaseDetailVO {
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private String category;
    private String visibility;
    private Long ownerId;
    private String ownerName;
    private Short status;
    private Integer documentCount;
    private Integer memberCount;
    private String memberRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
