package org.cc.enterpriseagent.document.dto;

import lombok.Data;

@Data
public class DocumentParseResponseVO {

    private Long documentId;

    private String status;

    private String title;

    private Integer charCount;

    private Integer sectionCount;
}