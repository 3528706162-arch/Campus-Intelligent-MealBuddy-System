package com.mealbuddy.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

public class AuthDTO {

    @Data
    public static class LoginRequest {
        @NotBlank(message = "邮箱不能为空")
        private String email;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "邮箱不能为空")
        private String email;
        @NotBlank(message = "密码不能为空")
        private String password;
        private String phone;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private Long userId;
        private String username;
        private String role;
    }
}
