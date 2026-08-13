package org.cc.enterpriseagent.document.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentPreviewVO {
    private Long id;
    private String name;
    private String originalName;
    private String fileType;
    private String content;
}
