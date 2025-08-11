package com.example.user_center.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 *
 * @author
 */
@Target(ElementType.METHOD) // 此注解作用于方法上
@Retention(RetentionPolicy.RUNTIME) // 此注解在运行时生效
public @interface AuthCheck {

    /**
     * 必须拥有的角色，默认为"admin"
     *
     * @return
     */
    int mustRole() default 1; // 根据您的项目，管理员角色值为1

}