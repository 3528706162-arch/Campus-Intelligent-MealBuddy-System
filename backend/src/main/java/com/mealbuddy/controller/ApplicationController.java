package com.mealbuddy.controller;

import com.mealbuddy.dto.GroupDTO.*;
import com.mealbuddy.dto.Result;
import com.mealbuddy.entity.Application;
import com.mealbuddy.service.ApplicationService;
import com.mealbuddy.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/{groupId}")
    public Result<Application> apply(@PathVariable Long groupId, @RequestBody(required = false) ApplicationRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        String remark = req != null ? req.getRemark() : null;
        return Result.ok("申请已提交", applicationService.apply(userId, groupId, remark));
    }

    @PutMapping("/{applicationId}/approve")
    public Result<Void> approve(@PathVariable Long applicationId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        applicationService.approveApplication(userId, applicationId);
        return Result.ok("已通过", null);
    }

    @PutMapping("/{applicationId}/reject")
    public Result<Void> reject(@PathVariable Long applicationId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        applicationService.rejectApplication(userId, applicationId);
        return Result.ok("已拒绝", null);
    }

    @DeleteMapping("/{applicationId}")
    public Result<Void> cancel(@PathVariable Long applicationId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        applicationService.cancelApplication(userId, applicationId);
        return Result.ok("已取消", null);
    }

    @GetMapping("/group/{groupId}")
    public Result<List<Application>> getGroupApplications(@PathVariable Long groupId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok(applicationService.getGroupApplications(groupId, userId));
    }

    @GetMapping("/my")
    public Result<List<Application>> myApplications() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok(applicationService.getMyApplications(userId));
    }
}
