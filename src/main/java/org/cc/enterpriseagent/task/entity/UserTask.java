package org.cc.enterpriseagent.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_task")
public class UserTask {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String description;
    private String status;
    private LocalDateTime dueAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean deleted;
}
