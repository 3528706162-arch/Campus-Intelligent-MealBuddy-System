package com.mealbuddy.service;

import com.mealbuddy.dto.AuthDTO.*;
import com.mealbuddy.entity.User;
import com.mealbuddy.exception.BusinessException;
import com.mealbuddy.repository.UserRepository;
import com.mealbuddy.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("BIZ_JOIN_003", "该邮箱已被注册", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("BIZ_JOIN_003", "该用户名已被使用", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role("USER")
                .build();
        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getRole());
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getUserId());
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole());
        return resp;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("AUTH_001", "账号或密码错误", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("AUTH_001", "账号或密码错误", HttpStatus.UNAUTHORIZED);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("AUTH_001", "账号已被封禁，请联系管理员", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getRole());
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getUserId());
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole());
        return resp;
    }
}
