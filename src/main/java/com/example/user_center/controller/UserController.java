package com.example.user_center.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.user_center.common.BaseResponse;
import com.example.user_center.common.ErrorCode;
import com.example.user_center.common.ResultUtils;
import com.example.user_center.exception.BusinessException;
import com.example.user_center.model.domain.User;
import com.example.user_center.model.request.UserLoginRequest;
import com.example.user_center.model.request.UserRegisterRequest;
import com.example.user_center.service.UserService;
import com.example.user_center.annotation.AuthCheck;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.user_center.constant.UserConstant.ADMIN_ROLE;
import static com.example.user_center.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 调用业务层完成注册逻辑
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user =  userService.userLogin(userLoginRequest, request);
        return ResultUtils.success(user);
    }
    @PostMapping("/logout")
    public BaseResponse<Void> userLogout(HttpServletRequest request) {
        if(request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.userLogout(request);
        return ResultUtils.success();
    }
    @GetMapping("/search")
    @AuthCheck
    public BaseResponse<List<User>> searchUsers(@RequestParam(name = "userAccount", required = false)String userAccount) {
        // 直接调用 Service 层封装好的方法
        List<User> safeUserList = userService.searchUsers(userAccount);
        return ResultUtils.success(safeUserList);
    }
    @DeleteMapping("/delete/{id}")
    @AuthCheck
    public BaseResponse<Boolean> deleteUser(@PathVariable long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }
    @GetMapping("/current")
    public BaseResponse<User> currentUser(HttpServletRequest  request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户不存在");
        }
        user = userService.getSafeUser(user);
        return ResultUtils.success(user);
    }
}