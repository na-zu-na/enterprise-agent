package org.cc.enterpriseagent.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.user.entity.SysUser;
import org.cc.enterpriseagent.user.mapper.SysUserMapper;
import org.cc.enterpriseagent.user.service.UserService;
import org.cc.enterpriseagent.user.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;

    public UserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public Result<List<UserInfoVO>> searchUsers(String keyword) {
        List<UserInfoVO> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .and(wrapper -> wrapper.like(SysUser::getUsername, keyword)
                                .or()
                                .like(SysUser::getNickname, keyword))
                        .orderByAsc(SysUser::getId))
                .stream()
                .map(this::toUserInfo)
                .toList();
        return new Result<>(users, "查询成功", 200);
    }

    private UserInfoVO toUserInfo(SysUser user) {
        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);
        return userInfo;
    }
}
