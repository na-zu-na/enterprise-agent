package org.cc.enterpriseagent.document.vo;

import lombok.Data;

import java.util.List;

@Data
public class RagResponseVO {

    private String answer;

    private List<CitationVO> citations;
}