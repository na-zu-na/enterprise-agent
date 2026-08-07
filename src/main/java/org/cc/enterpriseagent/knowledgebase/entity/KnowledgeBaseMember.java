package org.cc.enterpriseagent.knowledgebase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_base_member")
public class KnowledgeBaseMember {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long knowledgeBaseId;
    private Long userId;
    private String memberRole;
    private LocalDateTime joinedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean deleted;
}
