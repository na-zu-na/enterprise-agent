package org.cc.enterpriseagent.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RagAskRequestDTO {

    @NotBlank(message = "问题不能为空")
    private String query;

    private List<Long> knowledgeBaseIds;
}
