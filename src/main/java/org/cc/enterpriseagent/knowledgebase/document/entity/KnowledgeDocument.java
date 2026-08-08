package org.cc.enterpriseagent.knowledgebase.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_document")
public class KnowledgeDocument {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long knowledgeBaseId;
    private String name;
    private String originalName;
    private String fileType;
    private Long fileSize;
    private String storagePath;
    private String status;
    private Integer version;
    private Long uploadedBy;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean deleted;
}
