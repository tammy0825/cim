package com.crossoverjie.cim.route.pojo;

import java.io.Serializable;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2018/12/23 21:54
 * @since JDK 1.8
 */
public class RegisterInfoResDTO implements Serializable {
    private Long userId;
    private String userName;
    private String password;

    public RegisterInfoResDTO(Long userId, String userName, String password) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "RegisterInfoResDTO{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
