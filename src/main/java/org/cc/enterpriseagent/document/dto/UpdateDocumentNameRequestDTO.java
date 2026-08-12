package org.cc.enterpriseagent.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDocumentNameRequestDTO {

    @NotBlank(message = "文档名称不能为空")
    @Size(max = 255, message = "文档名称不能超过255个字符")
    private String name;
}
