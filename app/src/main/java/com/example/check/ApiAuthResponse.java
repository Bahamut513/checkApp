package com.example.check;

public class ApiAuthResponse {
    public boolean success;
    public String message;
    public String token;
    public User user;

    public static class User {
        public Long id;
        public String username;
        public String email;
    }
}