package org.cc.enterpriseagent.knowledgebase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_base")
public class KnowledgeBase {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String coverUrl;
    private String category;
    private String visibility;
    private Long ownerId;
    private Short status;
    private Integer documentCount;
    private Integer memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean deleted;
}
