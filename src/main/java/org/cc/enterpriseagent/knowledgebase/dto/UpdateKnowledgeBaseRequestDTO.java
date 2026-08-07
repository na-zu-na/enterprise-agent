package org.cc.enterpriseagent.knowledgebase.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateKnowledgeBaseRequestDTO {
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 128, message = "知识库名称不能超过128个字符")
    private String name;

    private String description;

    private String coverUrl;

    @NotBlank(message = "知识库分类不能为空")
    @Pattern(regexp = "GENERAL|POLICY|PROJECT|TECHNICAL", message = "知识库分类不合法")
    private String category;

    @NotBlank(message = "知识库可见范围不能为空")
    @Pattern(regexp = "PRIVATE|MEMBER|PUBLIC", message = "知识库可见范围不合法")
    private String visibility;

    @NotNull(message = "知识库状态不能为空")
    @Min(value = 0, message = "知识库状态不合法")
    @Max(value = 1, message = "知识库状态不合法")
    private Integer status;
}
