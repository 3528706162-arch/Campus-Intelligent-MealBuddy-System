package com.mealbuddy.service;

import com.mealbuddy.dto.GroupDTO.*;
import com.mealbuddy.entity.*;
import com.mealbuddy.exception.BusinessException;
import com.mealbuddy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupActivityRepository groupRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    @Transactional
    public GroupResponse createGroup(Long initiatorId, CreateRequest request) {
        if (!userService.canOperate(initiatorId, "CREATE_GROUP")) {
            throw new BusinessException("BIZ_CREDIT_004",
                    "您的信用分不足60分，无法创建约饭组", HttpStatus.FORBIDDEN);
        }

        User initiator = userRepository.findById(initiatorId).orElseThrow();
        GroupActivity group = GroupActivity.builder()
                .initiator(initiator)
                .title(request.getTitle())
                .canteen(request.getCanteen())
                .mealTime(request.getMealTime())
                .maxPeople(request.getMaxPeople())
                .remark(request.getRemark())
                .build();
        group = groupRepository.save(group);
        return toGroupResponse(group);
    }

    public Page<GroupResponse> getActiveGroups(int page, int size, String canteen) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<GroupActivity> groups;
        if (canteen != null && !canteen.isEmpty()) {
            groups = groupRepository.findByStatusAndCanteenContainingOrderByCreateTimeDesc("RECRUITING", canteen, pageRequest);
        } else {
            groups = groupRepository.findActiveGroups(pageRequest);
        }
        return groups.map(this::toGroupResponse);
    }

    public GroupResponse getGroupDetail(Long groupId) {
        GroupActivity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "约饭组不存在", HttpStatus.NOT_FOUND));
        return toGroupResponse(group);
    }

    @Transactional
    public void cancelGroup(Long userId, Long groupId) {
        GroupActivity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "约饭组不存在", HttpStatus.NOT_FOUND));

        if (!group.getInitiator().getUserId().equals(userId)) {
            throw new BusinessException("BIZ_JOIN_003", "只有发起人可以取消约饭组", HttpStatus.FORBIDDEN);
        }

        if (!"RECRUITING".equals(group.getStatus()) && !"CONFIRMED".equals(group.getStatus())) {
            throw new BusinessException("BIZ_JOIN_003", "当前状态不可取消", HttpStatus.BAD_REQUEST);
        }

        group.setStatus("CANCELLED");
        groupRepository.save(group);

        // 临近用餐时间取消,扣信用分
        if (group.getMealTime().minusHours(1).isBefore(LocalDateTime.now())) {
            userService.updateCreditScore(userId, new BigDecimal("-20"));
        }
    }

    @Transactional
    public void completeGroup(Long userId, Long groupId) {
        GroupActivity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "约饭组不存在", HttpStatus.NOT_FOUND));

        if (!group.getInitiator().getUserId().equals(userId)) {
            throw new BusinessException("BIZ_JOIN_003", "只有发起人可以完成约饭", HttpStatus.FORBIDDEN);
        }

        if (!"CONFIRMED".equals(group.getStatus())) {
            throw new BusinessException("BIZ_JOIN_003", "只有已组队状态才能完成", HttpStatus.BAD_REQUEST);
        }

        group.setStatus("FINISHED");
        groupRepository.save(group);
    }

    public List<GroupResponse> getMyGroups(Long userId) {
        return groupRepository.findByInitiatorUserId(userId).stream()
                .map(this::toGroupResponse).toList();
    }

    public List<Map<String, Object>> getGroupParticipants(Long groupId) {
        GroupActivity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "约饭组不存在", HttpStatus.NOT_FOUND));
        List<Map<String, Object>> participants = new java.util.ArrayList<>();

        // 包含发起人
        User initiator = group.getInitiator();
        Map<String, Object> initiatorMap = new HashMap<>();
        initiatorMap.put("userId", initiator.getUserId());
        initiatorMap.put("username", initiator.getUsername());
        initiatorMap.put("avatarUrl", initiator.getAvatarUrl());
        participants.add(initiatorMap);

        // 包含已通过的申请人
        List<Application> approvedApps = applicationRepository.findByGroupGroupIdAndStatus(groupId, "APPROVED");
        for (Application app : approvedApps) {
            User u = app.getUser();
            if (u.getUserId().equals(initiator.getUserId())) continue;
            Map<String, Object> participant = new HashMap<>();
            participant.put("userId", u.getUserId());
            participant.put("username", u.getUsername());
            participant.put("avatarUrl", u.getAvatarUrl());
            participants.add(participant);
        }

        return participants;
    }

    private GroupResponse toGroupResponse(GroupActivity g) {
        GroupResponse r = new GroupResponse();
        r.setGroupId(g.getGroupId());
        r.setTitle(g.getTitle());
        r.setCanteen(g.getCanteen());
        r.setMealTime(g.getMealTime());
        r.setMaxPeople(g.getMaxPeople());
        r.setCurrentPeople(g.getCurrentPeople());
        r.setStatus(g.getStatus());
        r.setRemark(g.getRemark());
        r.setInitiatorName(g.getInitiator().getUsername());
        r.setInitiatorId(g.getInitiator().getUserId());
        r.setCreateTime(g.getCreateTime());
        return r;
    }
}
