package com.mealbuddy.controller;

import com.mealbuddy.dto.GroupDTO.*;
import com.mealbuddy.dto.Result;
import com.mealbuddy.service.GroupService;
import com.mealbuddy.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public Result<GroupResponse> create(@Valid @RequestBody CreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok("创建成功", groupService.createGroup(userId, request));
    }

    @GetMapping
    public Result<Page<GroupResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String canteen) {
        return Result.ok(groupService.getActiveGroups(page, size, canteen));
    }

    @GetMapping("/{groupId}")
    public Result<GroupResponse> detail(@PathVariable Long groupId) {
        return Result.ok(groupService.getGroupDetail(groupId));
    }

    @DeleteMapping("/{groupId}")
    public Result<Void> cancel(@PathVariable Long groupId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        groupService.cancelGroup(userId, groupId);
        return Result.ok("已取消", null);
    }

    @PutMapping("/{groupId}/complete")
    public Result<Void> complete(@PathVariable Long groupId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        groupService.completeGroup(userId, groupId);
        return Result.ok("已完成", null);
    }

    @GetMapping("/my")
    public Result<?> myGroups() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok(groupService.getMyGroups(userId));
    }

    @GetMapping("/{groupId}/participants")
    public Result<?> participants(@PathVariable Long groupId) {
        return Result.ok(groupService.getGroupParticipants(groupId));
    }
}
