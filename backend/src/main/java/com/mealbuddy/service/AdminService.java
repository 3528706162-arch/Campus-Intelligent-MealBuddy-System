package com.mealbuddy.service;

import com.mealbuddy.entity.*;
import com.mealbuddy.exception.BusinessException;
import com.mealbuddy.repository.*;
import com.mealbuddy.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final GroupActivityRepository groupRepository;
    private final EvaluationRepository evaluationRepository;
    private final ApplicationRepository applicationRepository;
    private final AdminLogRepository adminLogRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalUsers = userRepository.count();
        long totalGroups = groupRepository.count();

        Page<GroupActivity> activeGroups = groupRepository.findActiveGroups(PageRequest.of(0, 10));
        long activeCount = activeGroups.getTotalElements();

        stats.put("totalUsers", totalUsers);
        stats.put("totalGroups", totalGroups);
        stats.put("activeGroups", activeCount);

        if (totalUsers > 0) {
            double activeRate = (double) activeCount * 2 / totalUsers; // 简化计算
            stats.put("activeRate", String.format("%.1f%%", Math.min(activeRate * 100, 100)));
        }

        return stats;
    }

    public Page<User> getUsers(int page, int size, String keyword) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.findByUsernameContainingOrEmailContaining(keyword, keyword, pr);
        }
        return userRepository.findAll(pr);
    }

    @Transactional
    public void banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("AUTH_001", "用户不存在", HttpStatus.NOT_FOUND));
        user.setStatus(0);
        userRepository.save(user);
        logOperation("BAN_USER", "USER", userId, "封禁用户: " + user.getUsername());
    }

    @Transactional
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("AUTH_001", "用户不存在", HttpStatus.NOT_FOUND));
        user.setStatus(1);
        userRepository.save(user);
        logOperation("UNBAN_USER", "USER", userId, "解封用户: " + user.getUsername());
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        GroupActivity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "约饭组不存在", HttpStatus.NOT_FOUND));
        group.setStatus("CANCELLED");
        groupRepository.save(group);
        logOperation("DELETE_GROUP", "GROUP", groupId, "取消约饭组: " + group.getTitle());
    }

    private void logOperation(String operationType, String targetType, Long targetId, String detail) {
        Long adminId = SecurityUtil.getCurrentUserId();
        if (adminId == null) adminId = 0L;
        AdminLog log = AdminLog.builder()
                .adminId(adminId)
                .operationType(operationType)
                .targetType(targetType)
                .targetId(targetId)
                .operationDetail(detail)
                .build();
        adminLogRepository.save(log);
    }

    public Page<GroupActivity> getAllGroups(int page, int size) {
        return groupRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));
    }
}
