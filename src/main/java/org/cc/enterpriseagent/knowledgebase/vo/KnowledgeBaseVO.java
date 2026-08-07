package org.cc.enterpriseagent.knowledgebase.vo;

import lombok.Data;

@Data
public class KnowledgeBaseVO {
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private String category;
    private String visibility;
    private Long ownerId;
    private Short status;
}
