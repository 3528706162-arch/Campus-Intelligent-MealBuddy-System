package com.mealbuddy.controller;

import com.mealbuddy.dto.Result;
import com.mealbuddy.entity.Notification;
import com.mealbuddy.service.NotificationService;
import com.mealbuddy.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<List<Notification>> list() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok(notificationService.getNotifications(userId));
    }

    @GetMapping("/unread")
    public Result<List<Notification>> unread() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/unread/count")
    public Result<Long> unreadCount() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.ok(null);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        notificationService.markAllAsRead(userId);
        return Result.ok(null);
    }
}
