package org.cc.enterpriseagent.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class SysLoginLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;
    private Short operationType;
    private Short result;
    private String failureReason;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
