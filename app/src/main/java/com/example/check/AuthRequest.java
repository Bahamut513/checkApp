package com.example.check;

public class AuthRequest {
    public String username;
    public String password;
    public String email;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public AuthRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}

// 登录请求
class LoginRequest extends AuthRequest {
    public LoginRequest(String username, String password) {
        super(username, password);
    }
}

// 注册请求
class RegisterRequest extends AuthRequest {
    public RegisterRequest(String username, String password, String email) {
        super(username, password, email);
    }
}

// 登录响应
class LoginResponse {
    public String token;
    public Long userId;
    public String username;
}