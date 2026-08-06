package org.cc.enterpriseagent.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cc.enterpriseagent.user.vo.UserInfoVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseVO {

    private String token;

    private UserInfoVO user;
}