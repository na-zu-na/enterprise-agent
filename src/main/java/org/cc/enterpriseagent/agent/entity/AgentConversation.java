package org.cc.enterpriseagent.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agent_conversation")
public class AgentConversation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean deleted;
}
