package org.cc.enterpriseagent.knowledgebase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cc.enterpriseagent.knowledgebase.entity.KnowledgeBaseMember;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseDetailVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseListVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseMemberVO;

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
            SELECT DISTINCT kb.id
            FROM knowledge_base kb
            INNER JOIN sys_user su ON su.id = #{userId}
                AND su.deleted = FALSE
            LEFT JOIN knowledge_base_member kbm
                ON kbm.knowledge_base_id = kb.id
                AND kbm.user_id = #{userId}
                AND kbm.deleted = FALSE
            WHERE kb.deleted = FALSE
              AND kb.status = 1
              AND (
                    su.role_code = 'SYSTEM_ADMIN'
                    OR kb.owner_id = #{userId}
                    OR (kb.visibility = 'PUBLIC' AND su.status = 1)
                    OR kbm.id IS NOT NULL
              )
            ORDER BY kb.id
            """)
    List<Long> selectAccessibleKnowledgeBaseIds(@Param("userId") Long userId);

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

    @Select("""
            SELECT EXISTS (
                SELECT 1
                FROM knowledge_base_member kbm
                INNER JOIN knowledge_base kb ON kb.id = kbm.knowledge_base_id
                WHERE kbm.knowledge_base_id = #{knowledgeBaseId}
                  AND kbm.user_id = #{userId}
                  AND kbm.deleted = FALSE
                  AND kb.deleted = FALSE
                  AND kb.status = 1
            )
            """)
    boolean hasKnowledgeBaseAccess(@Param("knowledgeBaseId") Long knowledgeBaseId, @Param("userId") Long userId);

    @Select("""
            SELECT kbm.user_id AS "userId", su.username, su.nickname, su.avatar_url AS "avatarUrl",
                   kbm.member_role AS "memberRole", kbm.joined_at AS "joinedAt"
            FROM knowledge_base_member kbm
            INNER JOIN sys_user su ON su.id = kbm.user_id
            WHERE kbm.knowledge_base_id = #{knowledgeBaseId}
              AND kbm.deleted = FALSE
              AND su.deleted = FALSE
            ORDER BY kbm.joined_at
            """)
    List<KnowledgeBaseMemberVO> selectKnowledgeBaseMembers(@Param("knowledgeBaseId") Long knowledgeBaseId);

    @Select("""
            SELECT id, knowledge_base_id AS "knowledgeBaseId", user_id AS "userId",
                   member_role AS "memberRole", joined_at AS "joinedAt", created_by AS "createdBy",
                   created_at AS "createdAt", updated_at AS "updatedAt", deleted
            FROM knowledge_base_member
            WHERE knowledge_base_id = #{knowledgeBaseId} AND user_id = #{userId}
            """)
    KnowledgeBaseMember selectMemberIncludingDeleted(@Param("knowledgeBaseId") Long knowledgeBaseId,
                                                     @Param("userId") Long userId);

    @Update("""
            UPDATE knowledge_base_member
            SET member_role = #{memberRole}, joined_at = CURRENT_TIMESTAMP, created_by = #{createdBy},
                updated_at = CURRENT_TIMESTAMP, deleted = FALSE
            WHERE id = #{memberId}
            """)
    int restoreMember(@Param("memberId") Long memberId, @Param("memberRole") String memberRole,
                      @Param("createdBy") Long createdBy);
}
