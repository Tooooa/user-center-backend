package com.example.user_center.service;

import com.example.user_center.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.user_center.model.request.UserLoginRequest;
import com.example.user_center.model.request.UserRegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 28489
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-07-23 12:09:01
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登陆
     * @param userLoginRequest
     * @return
     */
    User userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originalUser
     * @return
     */
    User getSafeUser(User originalUser);

    /**
     * 用户登出
     *
     * @param request
     */
    void userLogout(HttpServletRequest request);

    List<User> searchUsers(String userAccount);
}
