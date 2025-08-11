package com.example.user_center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.user_center.common.ErrorCode;
import com.example.user_center.constant.UserConstant;
import com.example.user_center.exception.BusinessException;
import com.example.user_center.mapper.UserMapper;
import com.example.user_center.model.domain.User;
import com.example.user_center.model.request.UserLoginRequest;
import com.example.user_center.model.request.UserRegisterRequest;
import com.example.user_center.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.user_center.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author tj
 */
@Service
@Slf4j // 使用 Lombok 添加日志功能，是 Spring Boot 项目的常用实践
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 盐值，用于密码加密，混淆原始密码。
     * 最佳实践：实际生产项目中，盐值应存储在更安全的位置，例如配置文件或密钥管理服务。
     */
    private static final String SALT = "yupi";

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        // 从请求对象中获取参数
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        // 1. 基础校验：检查所有参数是否为空或空白字符串
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,  planetCode)){
            // 抛出业务异常，指明参数错误
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册失败：输入参数存在空值。");
        }

        // 2. 业务规则校验
        // a. 账户长度不小于 4 位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册失败：账户长度小于4位。");
        }

        // b. 密码长度不小于 8 位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册失败：密码长度小于8位。");
        }

        // c. 密码和校验密码必须相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册失败：两次输入的密码不一致。");
        }

        // d. 账户不能包含特殊字符
        Pattern pattern = Pattern.compile(UserConstant.VALID_ACCOUNT_PATTERN_REGEX);
        Matcher matcher = pattern.matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册失败：账户包含特殊字符。");
        }
        //星球账户
        if(planetCode.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过短");
        }
        // e. 账户不能重复
        // QueryWrapper 是 MyBatis-Plus 的核心功能，用于构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册失败：账户已存在。");
        }
        // f. 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册失败：星球账户已存在。");
        }

        // 3. 对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 4. 向数据库插入新用户数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            // 记录日志，但不把数据库底层的具体问题暴露给前端
            log.error("用户注册失败：数据库插入操作异常。User: {}", user);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，请稍后重试");
        }

        // 5. 注册成功，返回新用户的 ID
        log.info("新用户 {} 注册成功，用户ID: {}", user.getUserAccount(), user.getId());
        return user.getId();
    }

    @Override
    public User userLogin(UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();


        // 1. 基础校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)){
            // 统一使用业务异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录失败：账户或密码为空。");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录失败：账户长度不符合规范。");
        }
        if (userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录失败：密码长度不符合规范。");
        }
        //账户不能包含特殊字符
        Pattern pattern = Pattern.compile(UserConstant.VALID_ACCOUNT_PATTERN_REGEX);
        Matcher matcher = pattern.matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录失败：账户名包含非法字符。");
        }

        // 2. 密码加密，用于和数据库中的密文进行比对
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.getOne(queryWrapper);

        // 校验用户是否存在
        if (user == null){
            // 使用 warn 级别日志记录登录失败的尝试
            log.warn("用户登录失败，账户或密码错误, userAccount: {}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户或密码错误");
        }

        // 4. 用户信息脱敏：创建一个安全的用户对象返回给前端
        User safeUser = getSafeUser(user);

        // 5. 记录用户的登录状态：将脱敏后的用户信息存入 Session
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE, safeUser);
        log.info("用户 {} 登录成功", safeUser.getUserAccount());
        return safeUser;
    }

    /**
     * 用户信息脱敏
     * @param originalUser 原始用户对象
     * @return 脱敏后的用户对象
     */
    @Override
    public User getSafeUser(User originalUser) {
        if(originalUser == null){
            return null;
        }
        User safeUser = new User();
        safeUser.setId(originalUser.getId());
        safeUser.setUserAccount(originalUser.getUserAccount());
        safeUser.setAvatarUrl(originalUser.getAvatarUrl());
        safeUser.setGender(originalUser.getGender());
        safeUser.setPhone(originalUser.getPhone());
        safeUser.setEmail(originalUser.getEmail());
        safeUser.setUserRole(originalUser.getUserRole());
        safeUser.setPlanetCode(originalUser.getPlanetCode());
        safeUser.setCreateTime(originalUser.getCreateTime());

        return safeUser;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        // 移除 Session 中的登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    @Override
    public List<User> searchUsers(String userAccount) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userAccount)) {
            queryWrapper.like("userAccount", userAccount);
        }
        // 调用 mapper 查询
        List<User> userList= this.list(queryWrapper);
        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());
    }
}