package org.cc.enterpriseagent.user.service;

import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.user.vo.UserInfoVO;

import java.util.List;

public interface UserService {

    Result<List<UserInfoVO>> searchUsers(String keyword);
}
