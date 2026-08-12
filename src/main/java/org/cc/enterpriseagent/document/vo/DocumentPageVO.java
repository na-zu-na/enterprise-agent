package org.cc.enterpriseagent.document.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DocumentPageVO {
    private List<DocumentListVO> records;
    private Long total;
    private Integer page;
    private Integer size;
}
