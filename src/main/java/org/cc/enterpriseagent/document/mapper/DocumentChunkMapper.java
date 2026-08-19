package org.cc.enterpriseagent.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.cc.enterpriseagent.document.entity.DocumentChunk;

@Mapper
public interface DocumentChunkMapper extends BaseMapper<DocumentChunk> {
    @Delete("DELETE FROM document_chunk WHERE document_id = #{documentId}")
    void deleteByDocumentId(@Param("documentId") Long documentId);

    @Update("""
        UPDATE document_chunk
        SET embedding = CAST(#{embedding} AS vector),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
          AND deleted = FALSE
    """)
    int updateEmbedding(@Param("id") Long id,
                        @Param("embedding")  String embedding);
}
