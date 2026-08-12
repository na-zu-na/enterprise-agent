package org.cc.enterpriseagent.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.cc.enterpriseagent.document.entity.KnowledgeDocument;
import org.cc.enterpriseagent.document.vo.DocumentListVO;
import org.cc.enterpriseagent.document.vo.DocumentDetailVO;

@Mapper
public interface DocumentMapper extends BaseMapper<KnowledgeDocument> {
    @Select("""
            SELECT kd.id, kd.knowledge_base_id AS "knowledgeBaseId", kb.name AS "knowledgeBaseName",
                   kd.name, kd.original_name AS "originalName", kd.file_type AS "fileType",
                   kd.file_size AS "fileSize", kd.status, kd.version,
                   kd.uploaded_by AS "uploadedBy", su.nickname AS "uploaderName",
                   kd.error_message AS "errorMessage", kd.created_at AS "createdAt",
                   kd.updated_at AS "updatedAt"
            FROM knowledge_document kd
            INNER JOIN knowledge_base kb ON kb.id = kd.knowledge_base_id AND kb.deleted = FALSE
            LEFT JOIN sys_user su ON su.id = kd.uploaded_by AND su.deleted = FALSE
            WHERE kd.id = #{documentId}
              AND kd.deleted = FALSE
            """)
    DocumentDetailVO selectDocumentDetail(@Param("documentId") Long documentId);

    @Select("""
            <script>
            SELECT kd.id, kd.knowledge_base_id AS \"knowledgeBaseId\", kd.name,
                   kd.original_name AS \"originalName\", kd.file_type AS \"fileType\",
                   kd.file_size AS \"fileSize\", kd.status, kd.version,
                   kd.uploaded_by AS \"uploadedBy\", su.nickname AS \"uploaderName\",
                   kd.created_at AS \"createdAt\", kd.updated_at AS \"updatedAt\"
            FROM knowledge_document kd
            LEFT JOIN sys_user su ON su.id = kd.uploaded_by AND su.deleted = FALSE
            WHERE kd.knowledge_base_id = #{knowledgeBaseId}
              AND kd.deleted = FALSE
            <if test='keyword != null and !keyword.isBlank()'>
              AND kd.name ILIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='status != null and !status.isBlank()'>
              AND kd.status = #{status}
            </if>
            <if test='fileType != null and !fileType.isBlank()'>
              AND kd.file_type = #{fileType}
            </if>
            ORDER BY kd.created_at DESC, kd.id DESC
            </script>
            """)
    IPage<DocumentListVO> selectDocumentPage(Page<DocumentListVO> page,
                                              @Param("knowledgeBaseId") Long knowledgeBaseId,
                                              @Param("keyword") String keyword,
                                              @Param("status") String status,
                                              @Param("fileType") String fileType);
}
