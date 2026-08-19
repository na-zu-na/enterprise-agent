package org.cc.enterpriseagent.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.auth.service.AuthService;
import org.cc.enterpriseagent.auth.vo.LoginResponseVO;
import org.cc.enterpriseagent.common.utils.JwtUtil;
import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.auth.dto.LoginRequestDTO;
import org.cc.enterpriseagent.user.entity.SysUser;
import org.cc.enterpriseagent.user.mapper.SysUserMapper;
import org.cc.enterpriseagent.user.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements AuthService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtil  jwtUtil;

    @Override
    @Transactional
    public Result<LoginResponseVO> login(LoginRequestDTO loginRequestDTO) {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginRequestDTO.getUsername()));

        if (sysUser == null) {
            return Result.error(404,"没有该用户");
        }

        LoginResponseVO loginResponseVO = new LoginResponseVO();

        if (sysUser.getPassword().equals(loginRequestDTO.getPassword())) {
            UserInfoVO userInfoVO = new UserInfoVO();
            BeanUtils.copyProperties(sysUser,userInfoVO);
            loginResponseVO.setUser(userInfoVO);
            loginResponseVO.setToken(jwtUtil.generateToken(userInfoVO));

            LocalDateTime now = LocalDateTime.now();
            sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, sysUser.getId())
                    .set(SysUser::getLastLoginAt, now)
                    .set(SysUser::getUpdatedAt, now));
        } else {
            return Result.error(401,"密码错误");
        }

        return Result.success(loginResponseVO);
    }
}
