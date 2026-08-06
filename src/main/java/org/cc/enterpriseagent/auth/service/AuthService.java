package org.cc.enterpriseagent.auth.service;

import org.cc.enterpriseagent.auth.vo.LoginResponseVO;
import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.dto.LoginRequestDTO;
import org.springframework.stereotype.Service;

public interface AuthService {
    Result<LoginResponseVO> login(LoginRequestDTO loginRequestDTO);
}
