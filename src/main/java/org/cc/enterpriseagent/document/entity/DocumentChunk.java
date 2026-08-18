package org.cc.enterpriseagent.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.cc.enterpriseagent.common.handler.PostgresJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "document_chunk", autoResultMap = true)
public class DocumentChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private Long knowledgeBaseId;

    private Integer chunkIndex;

    private String content;

    private Integer charCount;

    private String sectionTitle;

    private Integer sectionLevel;

    @TableField(value = "metadata", typeHandler = PostgresJsonbTypeHandler.class, jdbcType = JdbcType.OTHER)
    private Map<String, Object> metadata;

    private Integer documentVersion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Boolean deleted;
}
