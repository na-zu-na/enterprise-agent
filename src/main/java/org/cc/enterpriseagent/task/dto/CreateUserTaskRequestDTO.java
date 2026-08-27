package org.cc.enterpriseagent.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateUserTaskRequestDTO {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 255, message = "任务标题不能超过 255 个字符")
    private String title;

    private String description;

    private LocalDateTime dueAt;
}
