package com.mealbuddy.service;

import com.mealbuddy.entity.Application;
import com.mealbuddy.entity.GroupActivity;
import com.mealbuddy.entity.User;
import com.mealbuddy.exception.BusinessException;
import com.mealbuddy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final GroupActivityRepository groupRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public Application apply(Long userId, Long groupId, String remark) {
        if (!userService.canOperate(userId, "APPLY")) {
            throw new BusinessException("BIZ_CREDIT_004",
                    "您的信用分不足60分，无法申请加入", HttpStatus.FORBIDDEN);
        }

        GroupActivity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "约饭组不存在", HttpStatus.NOT_FOUND));

        if (!"RECRUITING".equals(group.getStatus())) {
            throw new BusinessException("BIZ_JOIN_003", "该约饭组已不再招募", HttpStatus.BAD_REQUEST);
        }

        if (group.getInitiator().getUserId().equals(userId)) {
            throw new BusinessException("BIZ_JOIN_003", "不能申请自己创建的约饭组", HttpStatus.BAD_REQUEST);
        }

        if (group.getCurrentPeople() >= group.getMaxPeople()) {
            throw new BusinessException("BIZ_JOIN_003", "该约饭组已满员", HttpStatus.CONFLICT);
        }

        if (applicationRepository.existsByGroupGroupIdAndUserUserIdAndStatus(groupId, userId, "PENDING")) {
            throw new BusinessException("BIZ_JOIN_003", "您已申请过，请等待审核", HttpStatus.CONFLICT);
        }

        // 时间冲突校验：检查用户在目标时间前后30分钟内是否有其他已通过的组局
        LocalDateTime conflictStart = group.getMealTime().minusMinutes(30);
        LocalDateTime conflictEnd = group.getMealTime().plusMinutes(30);
        List<Application> userApps = applicationRepository.findByUserUserIdAndStatus(userId, "APPROVED");
        for (Application userApp : userApps) {
            LocalDateTime appMealTime = userApp.getGroup().getMealTime();
            if (appMealTime != null && !appMealTime.isBefore(conflictStart) && !appMealTime.isAfter(conflictEnd)) {
                throw new BusinessException("BIZ_JOIN_003",
                        "该时段您已有其他约饭安排（前后30分钟内），请选择其他时间", HttpStatus.CONFLICT);
            }
        }

        User user = userRepository.findById(userId).orElseThrow();
        Application app = Application.builder()
                .group(group)
                .user(user)
                .remark(remark)
                .build();
        app = applicationRepository.save(app);

        notificationService.createNotification(
                group.getInitiator().getUserId(),
                "新的加入申请",
                user.getUsername() + " 申请加入 " + group.getTitle(),
                "APPLICATION");

        return app;
    }

    @Transactional
    public void approveApplication(Long initiatorId, Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "申请不存在", HttpStatus.NOT_FOUND));

        if (!app.getGroup().getInitiator().getUserId().equals(initiatorId)) {
            throw new BusinessException("BIZ_JOIN_003", "只有发起人可以审核", HttpStatus.FORBIDDEN);
        }

        if (!"PENDING".equals(app.getStatus())) {
            throw new BusinessException("BIZ_JOIN_003", "该申请已处理", HttpStatus.BAD_REQUEST);
        }

        GroupActivity group = app.getGroup();
        if (group.getCurrentPeople() >= group.getMaxPeople()) {
            throw new BusinessException("BIZ_JOIN_003", "约饭组已满员", HttpStatus.CONFLICT);
        }

        app.setStatus("APPROVED");
        applicationRepository.save(app);

        group.setCurrentPeople(group.getCurrentPeople() + 1);
        if (group.getCurrentPeople() >= group.getMaxPeople()) {
            group.setStatus("CONFIRMED");
        }
        groupRepository.save(group);

        notificationService.createNotification(
                app.getUser().getUserId(),
                "申请已通过",
                "您加入 " + group.getTitle() + " 的申请已通过",
                "APPROVAL");
    }

    @Transactional
    public void rejectApplication(Long initiatorId, Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "申请不存在", HttpStatus.NOT_FOUND));

        if (!app.getGroup().getInitiator().getUserId().equals(initiatorId)) {
            throw new BusinessException("BIZ_JOIN_003", "只有发起人可以审核", HttpStatus.FORBIDDEN);
        }

        if (!"PENDING".equals(app.getStatus())) {
            throw new BusinessException("BIZ_JOIN_003", "该申请已处理", HttpStatus.BAD_REQUEST);
        }

        app.setStatus("REJECTED");
        applicationRepository.save(app);

        notificationService.createNotification(
                app.getUser().getUserId(),
                "申请被拒绝",
                "您加入 " + app.getGroup().getTitle() + " 的申请未被通过",
                "REJECTION");
    }

    @Transactional
    public void cancelApplication(Long userId, Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "申请不存在", HttpStatus.NOT_FOUND));

        if (!app.getUser().getUserId().equals(userId)) {
            throw new BusinessException("BIZ_JOIN_003", "只能取消自己的申请", HttpStatus.FORBIDDEN);
        }

        if (!"PENDING".equals(app.getStatus()) && !"APPROVED".equals(app.getStatus())) {
            throw new BusinessException("BIZ_JOIN_003", "当前状态不可取消", HttpStatus.BAD_REQUEST);
        }

        if ("APPROVED".equals(app.getStatus())) {
            GroupActivity group = app.getGroup();
            if (group.getMealTime().minusHours(1).isBefore(LocalDateTime.now())) {
                userService.updateCreditScore(userId, new BigDecimal("-20"));
            }
            group.setCurrentPeople(group.getCurrentPeople() - 1);
            if (group.getStatus().equals("CONFIRMED")) {
                group.setStatus("RECRUITING");
            }
            groupRepository.save(group);
        }

        app.setStatus("CANCELLED");
        applicationRepository.save(app);
    }

    public List<Application> getGroupApplications(Long groupId, Long initiatorId) {
        GroupActivity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "约饭组不存在", HttpStatus.NOT_FOUND));
        if (!group.getInitiator().getUserId().equals(initiatorId)) {
            throw new BusinessException("BIZ_JOIN_003", "只有发起人可以查看", HttpStatus.FORBIDDEN);
        }
        return applicationRepository.findByGroupGroupId(groupId);
    }

    public List<Application> getMyApplications(Long userId) {
        return applicationRepository.findByUserUserId(userId);
    }
}
