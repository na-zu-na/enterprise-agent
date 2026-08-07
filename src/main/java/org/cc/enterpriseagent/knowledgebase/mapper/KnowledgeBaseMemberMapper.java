package org.cc.enterpriseagent.knowledgebase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.cc.enterpriseagent.knowledgebase.entity.KnowledgeBaseMember;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseDetailVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseListVO;

import java.util.List;

@Mapper
public interface KnowledgeBaseMemberMapper extends BaseMapper<KnowledgeBaseMember> {
    @Select("""
            SELECT kb.id, kb.name, kb.description, kb.cover_url AS "coverUrl",
                   kb.category, kb.visibility, kb.owner_id AS "ownerId", kb.status,
                   kb.document_count AS "documentCount", kb.member_count AS "memberCount",
                   kbm.member_role AS "memberRole", kb.created_at AS "createdAt"
            FROM knowledge_base_member kbm
            INNER JOIN knowledge_base kb ON kb.id = kbm.knowledge_base_id
            WHERE kbm.user_id = #{userId}
              AND kbm.deleted = FALSE
              AND kb.deleted = FALSE
              AND kb.status = 1
            ORDER BY kb.created_at DESC
            """)
    List<KnowledgeBaseListVO> selectAccessibleKnowledgeBases(@Param("userId") Long userId);

    @Select("""
            SELECT kb.id, kb.name, kb.description, kb.cover_url AS "coverUrl",
                   kb.category, kb.visibility, kb.owner_id AS "ownerId", owner.nickname AS "ownerName",
                   kb.status, kb.document_count AS "documentCount", kb.member_count AS "memberCount",
                   kbm.member_role AS "memberRole", kb.created_at AS "createdAt", kb.updated_at AS "updatedAt"
            FROM knowledge_base_member kbm
            INNER JOIN knowledge_base kb ON kb.id = kbm.knowledge_base_id
            INNER JOIN sys_user owner ON owner.id = kb.owner_id
            WHERE kbm.user_id = #{userId}
              AND kbm.knowledge_base_id = #{knowledgeBaseId}
              AND kbm.deleted = FALSE
              AND kb.deleted = FALSE
              AND kb.status = 1
              AND owner.deleted = FALSE
            """)
    KnowledgeBaseDetailVO selectAccessibleKnowledgeBaseDetail(
            @Param("knowledgeBaseId") Long knowledgeBaseId,
            @Param("userId") Long userId);
}
