package com.example.user_center.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登陆请求体
 * @author 28489
 */
@Data
public class UserLoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 4412980317709921227L;

    private String userAccount;

    private String userPassword;

}
