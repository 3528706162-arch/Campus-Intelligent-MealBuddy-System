package com.mealbuddy.controller;

import com.mealbuddy.dto.Result;
import com.mealbuddy.entity.GroupActivity;
import com.mealbuddy.entity.User;
import com.mealbuddy.service.AdminService;
import com.mealbuddy.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final NotificationService notificationService;

    @PostMapping("/announcement")
    public Result<?> createAnnouncement(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            return Result.fail("标题和内容不能为空");
        }
        // 给所有用户发送公告
        notificationService.broadcastAnnouncement(title, content);
        return Result.ok("公告已发布", null);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public Result<Page<User>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.getUsers(page, size, keyword));
    }

    @PutMapping("/users/{userId}/ban")
    public Result<Void> banUser(@PathVariable Long userId) {
        adminService.banUser(userId);
        return Result.ok("已封禁", null);
    }

    @PutMapping("/users/{userId}/unban")
    public Result<Void> unbanUser(@PathVariable Long userId) {
        adminService.unbanUser(userId);
        return Result.ok("已解封", null);
    }

    @DeleteMapping("/groups/{groupId}")
    public Result<Void> deleteGroup(@PathVariable Long groupId) {
        adminService.deleteGroup(groupId);
        return Result.ok("已取消", null);
    }

    @GetMapping("/groups")
    public Result<Page<GroupActivity>> groups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.getAllGroups(page, size));
    }
}
