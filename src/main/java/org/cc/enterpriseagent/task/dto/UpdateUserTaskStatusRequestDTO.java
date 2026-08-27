package org.cc.enterpriseagent.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserTaskStatusRequestDTO {

    @NotBlank(message = "任务状态不能为空")
    @Pattern(regexp = "TODO|DONE", message = "任务状态仅支持 TODO 或 DONE")
    private String status;
}
