package org.cc.enterpriseagent.auth.controller;

import jakarta.annotation.Resource;
import org.cc.enterpriseagent.auth.service.AuthService;
import org.cc.enterpriseagent.auth.vo.LoginResponseVO;
import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.auth.dto.LoginRequestDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    @Resource
    private AuthService authService;

    @PostMapping("/auth/login")
    public Result<LoginResponseVO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return authService.login(loginRequestDTO);
    }
}
