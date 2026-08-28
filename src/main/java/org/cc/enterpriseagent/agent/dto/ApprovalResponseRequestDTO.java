package org.cc.enterpriseagent.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApprovalResponseRequestDTO {

    @NotNull(message = "对话 ID 不能为空")
    @Positive(message = "对话 ID 必须大于 0")
    private Long conversationId;

    @NotBlank(message = "审批中断 ID 不能为空")
    private String interruptId;

    @NotNull(message = "审批结果不能为空")
    private Boolean approved;
}