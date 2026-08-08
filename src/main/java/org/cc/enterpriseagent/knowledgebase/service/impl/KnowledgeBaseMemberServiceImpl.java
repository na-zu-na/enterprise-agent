package org.cc.enterpriseagent.knowledgebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.common.UserContext;
import org.cc.enterpriseagent.knowledgebase.dto.AddKnowledgeBaseMemberRequestDTO;
import org.cc.enterpriseagent.knowledgebase.dto.UpdateKnowledgeBaseMemberRoleRequestDTO;
import org.cc.enterpriseagent.knowledgebase.entity.KnowledgeBase;
import org.cc.enterpriseagent.knowledgebase.entity.KnowledgeBaseMember;
import org.cc.enterpriseagent.knowledgebase.mapper.KnowledgeBaseMapper;
import org.cc.enterpriseagent.knowledgebase.mapper.KnowledgeBaseMemberMapper;
import org.cc.enterpriseagent.knowledgebase.service.KnowledgeBaseMemberService;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseMemberVO;
import org.cc.enterpriseagent.user.entity.SysUser;
import org.cc.enterpriseagent.user.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class KnowledgeBaseMemberServiceImpl extends ServiceImpl<KnowledgeBaseMemberMapper, KnowledgeBaseMember> implements KnowledgeBaseMemberService {
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Result<List<KnowledgeBaseMemberVO>> getKnowledgeBaseMembers(Long knowledgeBaseId) {
        Long userId = UserContext.getUserId();
        if (!baseMapper.hasKnowledgeBaseAccess(knowledgeBaseId, userId)) {
            return Result.error(404, "知识库不存在或无访问权限");
        }
        return new Result<>(baseMapper.selectKnowledgeBaseMembers(knowledgeBaseId), "查询成功", 200);
    }

    @Override
    @Transactional
    public Result<Void> addKnowledgeBaseMember(Long knowledgeBaseId, AddKnowledgeBaseMemberRequestDTO requestDTO) {
        Long currentUserId = UserContext.getUserId();

        boolean canManageKnowledgeBaseMemberRole = canManageKnowledgeBaseMemberRole(knowledgeBaseId);
        if (!canManageKnowledgeBaseMemberRole) {
            return Result.error(403,"暂无权限");
        }

        //查对象是否存在是否能加
        if (sysUserMapper.selectById(requestDTO.getUserId()) == null) {
            return Result.error(404, "用户不存在");
        }
        if (baseMapper.selectOne(new LambdaQueryWrapper<KnowledgeBaseMember>()
                .eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBaseId)
                .eq(KnowledgeBaseMember::getUserId, requestDTO.getUserId())) != null) {
            return Result.error(409, "该用户已是知识库成员");
        }

        //新增
        KnowledgeBaseMember deletedMember = baseMapper
                .selectMemberIncludingDeleted(knowledgeBaseId, requestDTO.getUserId());
        if (deletedMember == null) {
            KnowledgeBaseMember member = new KnowledgeBaseMember();
            member.setKnowledgeBaseId(knowledgeBaseId);
            member.setUserId(requestDTO.getUserId());
            member.setMemberRole(requestDTO.getMemberRole());
            member.setCreatedBy(currentUserId);
            baseMapper.insert(member);
        } else {
            baseMapper.restoreMember(deletedMember.getId(), requestDTO.getMemberRole(), currentUserId);
        }
        knowledgeBaseMapper.update(null, new LambdaUpdateWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getId, knowledgeBaseId)
                .setSql("member_count = member_count + 1")
                .setSql("updated_at = CURRENT_TIMESTAMP"));
        return new Result<>(null, "添加成功", 200);
    }

    @Override
    @Transactional
    public Result<Void> updateKnowledgeBaseMemberRole(Long knowledgeBaseId, Long userId,
                                                       UpdateKnowledgeBaseMemberRoleRequestDTO requestDTO) {
        boolean canManageKnowledgeBaseMemberRole = canManageKnowledgeBaseMemberRole(knowledgeBaseId);
        if (!canManageKnowledgeBaseMemberRole) {
            return Result.error(403,"暂无权限");
        }

        //查成员
        KnowledgeBaseMember member = baseMapper.selectOne(new LambdaQueryWrapper<KnowledgeBaseMember>()
                .eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBaseId)
                .eq(KnowledgeBaseMember::getUserId, userId));
        if (member == null) {
            return Result.error(404, "知识库成员不存在");
        }

        member.setMemberRole(requestDTO.getMemberRole());
        member.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(member);
        return new Result<>(null, "修改成功", 200);
    }

    @Override
    @Transactional
    public Result<Void> removeKnowledgeBaseMember(Long knowledgeBaseId, Long userId) {
        boolean canManageKnowledgeBaseMemberRole = canManageKnowledgeBaseMemberRole(knowledgeBaseId);
        if (!canManageKnowledgeBaseMemberRole) {
            return Result.error(403,"暂无权限");
        }

        KnowledgeBaseMember member = baseMapper.selectOne(new LambdaQueryWrapper<KnowledgeBaseMember>()
                .eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBaseId)
                .eq(KnowledgeBaseMember::getUserId, userId));
        if (member == null) {
            return Result.error(404, "知识库成员不存在");
        }

        baseMapper.deleteById(member.getId());
        knowledgeBaseMapper.update(null, new LambdaUpdateWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getId, knowledgeBaseId)
                .setSql("member_count = GREATEST(member_count - 1, 0)")
                .setSql("updated_at = CURRENT_TIMESTAMP"));
        return new Result<>(null, "移除成功", 200);
    }

    //判断权限
    public boolean canManageKnowledgeBaseMemberRole(Long knowledgeBaseId) {
        Long currentUserId = UserContext.getUserId();
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            return false;
        }

        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        boolean isOwner = currentUserId != null && currentUserId.equals(knowledgeBase.getOwnerId());
        boolean isSystemAdmin = currentUser != null && "SYSTEM_ADMIN".equals(currentUser.getRoleCode());
        boolean isKnowledgeBaseAdmin = baseMapper.selectOne(new LambdaQueryWrapper<KnowledgeBaseMember>()
                .eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBaseId)
                .eq(KnowledgeBaseMember::getUserId, currentUserId)
                .eq(KnowledgeBaseMember::getMemberRole, "ADMIN")) != null;
        return isOwner || isKnowledgeBaseAdmin || isSystemAdmin;
    }
}
