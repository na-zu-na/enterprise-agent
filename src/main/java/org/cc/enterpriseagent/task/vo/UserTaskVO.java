package org.cc.enterpriseagent.task.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserTaskVO {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String status;
    private LocalDateTime dueAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
