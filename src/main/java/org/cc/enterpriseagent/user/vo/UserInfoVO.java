package org.cc.enterpriseagent.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatarUrl;

    /**
     * 角色编码列表
     */
    private String roleCode;
}