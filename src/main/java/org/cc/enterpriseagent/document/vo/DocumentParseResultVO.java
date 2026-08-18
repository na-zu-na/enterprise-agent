package org.cc.enterpriseagent.document.vo;

import lombok.Data;

@Data
public class DocumentParseResultVO {

    private Long documentId;

    private String status;

    private Integer chunkCount;
}