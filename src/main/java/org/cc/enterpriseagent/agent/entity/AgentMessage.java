package org.cc.enterpriseagent.agent.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.cc.enterpriseagent.common.handler.PostgresJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "agent_message", autoResultMap = true)
public class AgentMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long conversationId;
    private String role;
    private String content;

    @TableField(value = "citations", typeHandler = PostgresJsonbTypeHandler.class, jdbcType = JdbcType.OTHER)
    private Map<String, Object> citations;

    @TableField(value = "checkpoint", typeHandler = PostgresJsonbTypeHandler.class, jdbcType = JdbcType.OTHER)
    private Map<String, Object> checkpoint;

    private String status;

    @TableField(value = "approval", typeHandler = PostgresJsonbTypeHandler.class, jdbcType = JdbcType.OTHER)
    private Map<String, Object> approval;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean deleted;
}
