package com.example.user_center.aop;

import com.example.user_center.annotation.AuthCheck;
import com.example.user_center.common.ErrorCode;
import com.example.user_center.exception.BusinessException;
import com.example.user_center.model.domain.User;
import com.example.user_center.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.example.user_center.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 权限校验 AOP
 *
 * @author
 */
@Aspect
@Component
public class AuthAspect {

    @Autowired
    private UserService userService;

    /**
     * 定义环绕通知，拦截所有包含 @AuthCheck 注解的方法
     *
     * @param joinPoint 切点，代表被拦截的方法
     * @param authCheck 注解本身，可以从中获取参数
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doAuthCheck(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 从注解中获取必须的角色
        int mustRole = authCheck.mustRole();

        // 获取当前请求的 request 对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取当前登录用户
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;

        // 校验
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (currentUser.getUserRole() != mustRole) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 权限校验通过，执行原始方法
        return joinPoint.proceed();
    }
}