package com.example.user_center.constant;

public interface UserConstant {
    /**
     * 用户登录状态键
     */
    String USER_LOGIN_STATE = "userLoginState";
    /**
     * 校验账户名是否包含特殊字符的正则表达式
     */
    String VALID_ACCOUNT_PATTERN_REGEX = "\\pP|\\pS|\\s+";
    // 权限
    /**
     * 默认权限
     */
    int DEFAULT_ROLE = 0;
    /**
     * 管理员权限
     */
    int ADMIN_ROLE = 1;
}
