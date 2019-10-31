package com.hpy.controller.backend;

import com.hpy.common.Const;
import com.hpy.common.ResponseResult;
import com.hpy.pojo.User;
import com.hpy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * Author: hpy
 * Date: 2019-10-30
 * Description: <描述>
 */
@RestController
@RequestMapping("/manager/user")
public class UserMapperController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseResult login(HttpSession session, String username, String password) {
        ResponseResult response = userService.login(username, password);
        if (response.isSuccess()) {
            // 判断是否为管理员
            User user = (User) response.getData();
            if (Const.Role.ROLE_ADMIN != user.getRole()) {
                return ResponseResult.createByError("不是管理员，登陆失败");
            }
            session.setAttribute(Const.CURRENT_USER, user);
        }
        return response;
    }

}
