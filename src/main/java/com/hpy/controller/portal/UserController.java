package com.hpy.controller.portal;

import com.hpy.common.Const;
import com.hpy.common.ResponseCode;
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
 * Date: 2019-10-29
 * Description: <描述>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseResult login(String username, String password, HttpSession session) {
        ResponseResult response = userService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    @RequestMapping("/logout")
    public ResponseResult logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ResponseResult.createBySuccess("退出成功");
    }

    @PostMapping("/register")
    public ResponseResult register(User user) {
        return userService.register(user);
    }

    @RequestMapping("/check_valid")
    public ResponseResult checkValid(String str, String type) {
        return userService.checkValid(str, type);
    }

    @RequestMapping("/get_user_info")
    public ResponseResult getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return ResponseResult.createBySuccess(user);
        }
        return ResponseResult.createByError("用户未登录,无法获取当前用户信息");
    }

    @PostMapping("/forget_get_question")
    public ResponseResult forgetGetQuestion(String username) {
        return userService.forgetGetQuestion(username);
    }

    @PostMapping("/forget_check_answer")
    public ResponseResult forgetCheckAnswer(String username, String question, String answer) {
        return userService.forgetCheckAnswer(username, question, answer);
    }

    @PostMapping("/forget_reset_password")
    public ResponseResult forgetResetPassword(String username, String passwordNew, String token) {
        return userService.forgetResetPassword(username, passwordNew, token);
    }

    @PostMapping("/reset_password")
    public ResponseResult resetPassword(HttpSession session, String passwordOld, String passwordNew) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return userService.resetPassword(user, passwordOld, passwordNew);
    }

    @PostMapping("/update_information")
    public ResponseResult updateInformation(HttpSession session, User updateUser) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        ResponseResult response = userService.updateInformation(currentUser, updateUser);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    @PostMapping("/get_information")
    public ResponseResult getInformation(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return userService.getInformation(user.getId());
    }

}
