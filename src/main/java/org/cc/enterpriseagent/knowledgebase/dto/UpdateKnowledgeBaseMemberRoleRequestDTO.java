package org.cc.enterpriseagent.knowledgebase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateKnowledgeBaseMemberRoleRequestDTO {
    @NotBlank(message = "成员角色不能为空")
    @Pattern(regexp = "VIEWER|EDITOR|ADMIN", message = "成员角色不合法")
    private String memberRole;
}
