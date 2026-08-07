package org.cc.enterpriseagent.knowledgebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.common.UserContext;
import org.cc.enterpriseagent.knowledgebase.dto.CreateKnowledgeBaseRequestDTO;
import org.cc.enterpriseagent.knowledgebase.dto.UpdateKnowledgeBaseRequestDTO;
import org.cc.enterpriseagent.knowledgebase.entity.KnowledgeBase;
import org.cc.enterpriseagent.knowledgebase.entity.KnowledgeBaseMember;
import org.cc.enterpriseagent.knowledgebase.mapper.KnowledgeBaseMapper;
import org.cc.enterpriseagent.knowledgebase.mapper.KnowledgeBaseMemberMapper;
import org.cc.enterpriseagent.knowledgebase.service.KnowledgeBaseService;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseDetailVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseListVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseVO;
import org.cc.enterpriseagent.user.entity.SysUser;
import org.cc.enterpriseagent.user.mapper.SysUserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper,KnowledgeBase> implements KnowledgeBaseService {
    @Autowired
    private KnowledgeBaseMemberMapper knowledgeBaseMemberMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    @Transactional
    public Result<KnowledgeBaseVO> createKnowledgeBase(CreateKnowledgeBaseRequestDTO requestDTO) {
        Long ownerId = UserContext.getUserId();

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        BeanUtils.copyProperties(requestDTO, knowledgeBase);
        knowledgeBase.setOwnerId(ownerId);
        knowledgeBase.setStatus((short) 1);
        knowledgeBase.setDocumentCount(0);
        knowledgeBase.setMemberCount(1);
        baseMapper.insert(knowledgeBase);

        KnowledgeBaseMember ownerMember = new KnowledgeBaseMember();
        ownerMember.setKnowledgeBaseId(knowledgeBase.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setMemberRole("ADMIN");
        ownerMember.setCreatedBy(ownerId);
        knowledgeBaseMemberMapper.insert(ownerMember);

        KnowledgeBaseVO knowledgeBaseVO = new KnowledgeBaseVO();
        BeanUtils.copyProperties(knowledgeBase, knowledgeBaseVO);
        return Result.success(knowledgeBaseVO);
    }

    @Override
    public Result<List<KnowledgeBaseListVO>> getAccessibleKnowledgeBases() {
        Long userId = UserContext.getUserId();
        List<KnowledgeBaseListVO> knowledgeBases = knowledgeBaseMemberMapper.selectAccessibleKnowledgeBases(userId);
        return new Result<>(knowledgeBases, "查询成功", 200);
    }

    @Override
    public Result<KnowledgeBaseDetailVO> getKnowledgeBaseDetail(Long knowledgeBaseId) {
        KnowledgeBaseDetailVO knowledgeBaseDetail = knowledgeBaseMemberMapper
                .selectAccessibleKnowledgeBaseDetail(knowledgeBaseId, UserContext.getUserId());
        if (knowledgeBaseDetail == null) {
            return Result.error(404, "知识库不存在或无访问权限");
        }
        return new Result<>(knowledgeBaseDetail, "查询成功", 200);
    }

    @Override
    @Transactional
    public Result<Void> updateKnowledgeBase(Long knowledgeBaseId, UpdateKnowledgeBaseRequestDTO requestDTO) {
        Long userId = UserContext.getUserId();

        //查数据库
        KnowledgeBase knowledgeBase = baseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            return Result.error(404, "知识库不存在");
        }

        //查用户
        SysUser currentUser = sysUserMapper.selectById(userId);
        boolean isOwner = false;
        if (userId != null) {
            isOwner = userId.equals(knowledgeBase.getOwnerId());
        }
        boolean isSystemAdmin = currentUser != null && "SYSTEM_ADMIN".equals(currentUser.getRoleCode());

        //查用户是否有权限修改数据库
        boolean isKnowledgeBaseAdmin = knowledgeBaseMemberMapper.selectOne(
                new LambdaQueryWrapper<KnowledgeBaseMember>()
                        .eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBaseId)
                        .eq(KnowledgeBaseMember::getUserId, userId)
                        .eq(KnowledgeBaseMember::getMemberRole, "ADMIN")) != null;
        if (!isOwner && !isKnowledgeBaseAdmin && !isSystemAdmin) {
            return Result.error(403, "无修改知识库权限");
        }

        BeanUtils.copyProperties(requestDTO, knowledgeBase);
        knowledgeBase.setStatus(requestDTO.getStatus().shortValue());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(knowledgeBase);
        return new Result<>(null, "修改成功", 200);
    }

    @Override
    @Transactional
    public Result<Void> deleteKnowledgeBase(Long knowledgeBaseId) {
        Long userId = UserContext.getUserId();
        KnowledgeBase knowledgeBase = baseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            return Result.error(404, "知识库不存在");
        }

        SysUser currentUser = sysUserMapper.selectById(userId);
        boolean isOwner = userId != null && userId.equals(knowledgeBase.getOwnerId());
        boolean isSystemAdmin = currentUser != null && "SYSTEM_ADMIN".equals(currentUser.getRoleCode());
        if (!isOwner && !isSystemAdmin) {
            return Result.error(403, "无删除知识库权限");
        }

        //执行逻辑删除而非物理删除
        baseMapper.deleteById(knowledgeBaseId);
        return new Result<>(null, "删除成功", 200);
    }
}
