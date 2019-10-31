package com.hpy.service.impl;

import com.hpy.common.Const;
import com.hpy.common.ResponseCode;
import com.hpy.common.ResponseResult;
import com.hpy.common.TokenCache;
import com.hpy.dao.UserMapper;
import com.hpy.pojo.User;
import com.hpy.service.UserService;
import com.hpy.vo.PasswordVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Author: hpy
 * Date: 2019-10-29
 * Description: <描述>
 */
@Service("/userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordService passwordService;

    @Override
    public ResponseResult login(String username, String password) {
        // 根据用户名查询出用户
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            // 用户存在，判断密码
            PasswordVO passwordVO = new PasswordVO(password, username);
            String encryptPassword = passwordService.encryptPassword(passwordVO);
            if (StringUtils.equals(user.getPassword(), encryptPassword)) {
                // 密码正确，密码置空
                user.setPassword(StringUtils.EMPTY);
                return ResponseResult.createBySuccess("登陆成功", user);
            }
        }
        return ResponseResult.createByError("登陆失败，用户名或密码不正确");
    }

    @Override
    @Transactional
    public ResponseResult register(User user) {
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        // 校验用户名和邮箱是否已被注册
        ResponseResult response = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!response.isSuccess()) {
            return response;
        }
        response = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!response.isSuccess()) {
            return response;
        }

        // 用户名和密码都未被注册，注册用户
        PasswordVO passwordVO = new PasswordVO(user.getPassword(), user.getUsername());
        String encryptPassword = passwordService.encryptPassword(passwordVO);
        user.setPassword(encryptPassword);

        // 设置角色
        user.setRole(Const.Role.ROLE_CUSTOMER);

        int resultCount = userMapper.insert(user);
        if (resultCount > 0) {
            return ResponseResult.createBySuccess("注册成功");
        }
        return ResponseResult.createByError("注册失败");
    }

    @Override
    public ResponseResult checkValid(String str, String type) {
        if (StringUtils.isBlank(str) || StringUtils.isBlank(type)) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 校验用户名
        if (Const.USERNAME.equals(type)) {
            int count = userMapper.checkByUsername(str);
            if (count > 0) {
                return ResponseResult.createByError("用户已存在");
            }
        }
        // 校验邮箱
        if (Const.EMAIL.equals(type)) {
            int count = userMapper.checkByEmail(str);
            if (count > 0) {
                return ResponseResult.createByError("邮箱已存在");
            }
        }
        return ResponseResult.createBySuccess();
    }

    @Override
    public ResponseResult forgetGetQuestion(String username) {
        if (StringUtils.isBlank(username)) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return ResponseResult.createByError("该用户不存在");
        }
        if (StringUtils.isBlank(user.getQuestion())) {
            return ResponseResult.createByError("该用户未设置找回密码问题");
        }
        return ResponseResult.createBySuccess(user.getQuestion());
    }

    @Override
    public ResponseResult forgetCheckAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            // 问题和问题答案是该用户的，并且是正确的, 返回给前台 Token
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            // token 存入缓存中，并返回给前台
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, token);
            return ResponseResult.createBySuccess(token);
        }
        return ResponseResult.createByError("问题答案错误");
    }

    @Override
    @Transactional
    public ResponseResult forgetResetPassword(String username, String passwordNew, String token) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(passwordNew) || StringUtils.isBlank(token)) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        String cacheToken = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(cacheToken)) {
            return ResponseResult.createByError("Token已过期，请重新获取Token");
        }
        if (token.equals(cacheToken)) {
            // Token正确，将Token设置为空，防止使用该Token重复修改密码
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, StringUtils.EMPTY);
            // 修改密码
            PasswordVO passwordVO = new PasswordVO(passwordNew, username);
            String encryptPassword = passwordService.encryptPassword(passwordVO);
            int updateCount = userMapper.updatePasswordByUsername(encryptPassword, username);
            if (updateCount > 0) {
                return ResponseResult.createBySuccess("密码更新成功");
            }
            return ResponseResult.createByError("密码更新失败");
        }
        return ResponseResult.createByError("token不匹配，密码修改失败");
    }

    @Override
    @Transactional
    public ResponseResult resetPassword(User user, String passwordOld, String passwordNew) {
        if (user == null || StringUtils.isBlank(passwordOld) || StringUtils.isBlank(passwordNew)) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 校验旧密码
        PasswordVO passwordVO = new PasswordVO(passwordOld, user.getUsername());
        String encryptPassword = passwordService.encryptPassword(passwordVO);
        int resultCount = userMapper.checkPasswordByUserId(encryptPassword, user.getId());
        if (resultCount == 0) {
            return ResponseResult.createByError("旧密码错误");
        }
        // 旧密码正确，修改密码
        passwordVO = new PasswordVO(passwordNew, user.getUsername());
        encryptPassword = passwordService.encryptPassword(passwordVO);
        int updateCount = userMapper.updatePasswordByUserId(encryptPassword, user.getId());
        if (updateCount > 0) {
            return ResponseResult.createBySuccess("密码修改成功");
        }
        return ResponseResult.createByError("密码修改失败");
    }

    @Override
    public ResponseResult updateInformation(User currentUser, User updateUser) {
        if (currentUser == null || updateUser == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 校验要修改的邮箱
        int resultCount = userMapper.checkEmailByUserId(updateUser.getEmail(), currentUser.getId());
        if (resultCount > 0) {
            return ResponseResult.createByError("邮箱已存在，请更换邮箱再重试");
        }
        // 限制可以更新的字段
        User user = new User();
        user.setId(currentUser.getId());
        user.setPhone(updateUser.getEmail());
        user.setEmail(updateUser.getPhone());
        user.setQuestion(updateUser.getQuestion());
        user.setAnswer(updateUser.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            // 字段更新成功，填充其余字段
            user.setUsername(currentUser.getUsername());
            user.setPassword(currentUser.getPassword());
            user.setRole(currentUser.getRole());
            user.setCreateTime(currentUser.getCreateTime());
            user.setUpdateTime(currentUser.getUpdateTime());
            return ResponseResult.createBySuccess("个人信息更新成功", user);
        }
        return ResponseResult.createByError("个人信息更新失败");
    }

    @Override
    public ResponseResult getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ResponseResult.createByError("用户信息获取失败");
        }
        user.setPassword(StringUtils.EMPTY);
        return ResponseResult.createBySuccess(user);
    }

}
