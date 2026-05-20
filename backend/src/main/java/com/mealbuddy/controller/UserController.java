package com.mealbuddy.controller;

import com.mealbuddy.dto.Result;
import com.mealbuddy.entity.User;
import com.mealbuddy.entity.UserPreference;
import com.mealbuddy.service.UserService;
import com.mealbuddy.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${app.upload.base-path:${user.home}/mealbuddy/uploads}")
    private String uploadBasePath;

    @GetMapping("/profile")
    public Result<User> getProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok(userService.getUserById(userId));
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        User updated = userService.updateProfile(userId,
                body.get("username"),
                body.get("phone"));
        return Result.ok("个人信息已更新", updated);
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        if (file.isEmpty()) return Result.fail("请选择文件");

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadBasePath, "avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            String filename = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            // 更新用户头像URL
            String avatarUrl = "/uploads/avatars/" + filename;
            userService.updateAvatar(userId, avatarUrl);

            return Result.ok("头像上传成功", avatarUrl);
        } catch (IOException e) {
            return Result.fail("头像上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/preference")
    public Result<UserPreference> getPreference() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok(userService.getPreference(userId));
    }

    @PutMapping("/preference")
    public Result<UserPreference> updatePreference(@RequestBody Map<String, String> pref) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok("偏好更新成功", userService.updatePreference(userId, pref));
    }
}
