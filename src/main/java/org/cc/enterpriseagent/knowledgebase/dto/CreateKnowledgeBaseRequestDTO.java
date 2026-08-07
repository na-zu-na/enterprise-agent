package org.cc.enterpriseagent.knowledgebase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateKnowledgeBaseRequestDTO {
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 128, message = "知识库名称不能超过128个字符")
    private String name;

    @Size(max = 1000, message = "知识库描述不能超过1000个字符")
    private String description;

    @Size(max = 500, message = "封面地址不能超过500个字符")
    private String coverUrl;

    @NotBlank(message = "知识库分类不能为空")
    @Pattern(regexp = "GENERAL|POLICY|PROJECT|TECHNICAL", message = "知识库分类不合法")
    private String category;

    @NotBlank(message = "知识库可见范围不能为空")
    @Pattern(regexp = "PRIVATE|MEMBER|PUBLIC", message = "知识库可见范围不合法")
    private String visibility;
}
