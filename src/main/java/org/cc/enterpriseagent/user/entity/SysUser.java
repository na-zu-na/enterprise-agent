package org.cc.enterpriseagent.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    /**
     * 用户角色：
     * USER              普通用户
     * KNOWLEDGE_ADMIN   知识库管理员
     * SYSTEM_ADMIN      系统管理员
     */
    private String roleCode;

    /**
     * 用户状态：
     * 0 禁用
     * 1 正常
     * 2 锁定
     */
    private Short status;

    private String avatarUrl;

    private LocalDateTime lastLoginAt;

    private String lastLoginIp;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 逻辑删除：
     * false 未删除
     * true 已删除
     */
    @TableLogic(value = "false",delval = "true")
    private Boolean deleted;
}
