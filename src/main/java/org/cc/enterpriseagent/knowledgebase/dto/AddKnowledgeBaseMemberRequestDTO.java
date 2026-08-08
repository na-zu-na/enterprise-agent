package org.cc.enterpriseagent.knowledgebase.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddKnowledgeBaseMemberRequestDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "成员角色不能为空")
    @Pattern(regexp = "VIEWER|EDITOR|ADMIN", message = "成员角色不合法")
    private String memberRole;
}
