package com.hpy.common;

/**
 * Author: hpy
 * Date: 2019-10-29
 * Description: <描述>
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public interface Role {
        Integer ROLE_CUSTOMER = 0;  // 普通用户
        Integer ROLE_ADMIN = 1;     // 管理员
    }

}
