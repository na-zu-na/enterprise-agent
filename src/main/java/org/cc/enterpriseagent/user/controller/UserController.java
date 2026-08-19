package org.cc.enterpriseagent.user.controller;

import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.user.service.UserService;
import org.cc.enterpriseagent.user.vo.UserInfoVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    public Result<List<UserInfoVO>> searchUsers(@RequestParam String keyword) {
        return userService.searchUsers(keyword);
    }
}
