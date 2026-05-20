package com.mealbuddy.service;

import com.mealbuddy.entity.Evaluation;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final GroupActivityRepository groupRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public Evaluation submitEvaluation(Long fromUserId, Long groupId, Long toUserId,
                                        BigDecimal rating, String content, Boolean anonymous) {
        GroupActivity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("BIZ_JOIN_003", "约饭组不存在", HttpStatus.NOT_FOUND));

        if (!"FINISHED".equals(group.getStatus())) {
            throw new BusinessException("BIZ_JOIN_003", "约饭完成后才能评价", HttpStatus.BAD_REQUEST);
        }

        // 48小时评价窗口
        if (group.getCreateTime().plusHours(48).isBefore(LocalDateTime.now())) {
            throw new BusinessException("BIZ_JOIN_003", "评价时间已过，需在48小时内完成评价", HttpStatus.BAD_REQUEST);
        }

        if (fromUserId.equals(toUserId)) {
            throw new BusinessException("BIZ_JOIN_003", "不能评价自己", HttpStatus.BAD_REQUEST);
        }

        if (evaluationRepository.existsByGroupGroupIdAndFromUserUserIdAndToUserUserId(groupId, fromUserId, toUserId)) {
            throw new BusinessException("BIZ_JOIN_003", "您已评价过该用户", HttpStatus.CONFLICT);
        }

        User fromUser = userRepository.findById(fromUserId).orElseThrow();
        User toUser = userRepository.findById(toUserId).orElseThrow();

        Evaluation evaluation = Evaluation.builder()
                .group(group)
                .fromUser(fromUser)
                .toUser(toUser)
                .rating(rating)
                .content(content)
                .anonymous(anonymous != null && anonymous)
                .build();
        evaluation = evaluationRepository.save(evaluation);

        // 根据评分调整信用分
        BigDecimal scoreChange;
        if (rating.compareTo(new BigDecimal("4.0")) >= 0) {
            scoreChange = new BigDecimal("2.0");
        } else if (rating.compareTo(new BigDecimal("2.0")) <= 0) {
            scoreChange = new BigDecimal("-3.0");
        } else {
            scoreChange = BigDecimal.ZERO;
        }
        userService.updateCreditScore(toUserId, scoreChange);

        // 通知被评价用户
        String evalTitle = "收到新评价";
        String evalContent = (anonymous != null && anonymous ? "匿名用户" : fromUser.getUsername())
                + " 在 " + group.getTitle() + " 中给了您 " + rating + " 星评价";
        if (content != null && !content.isBlank()) {
            evalContent += "：" + (content.length() > 50 ? content.substring(0, 50) + "..." : content);
        }
        notificationService.createNotification(toUserId, evalTitle, evalContent,
                "EVALUATION", groupId);

        // 通知信用分变化
        if (scoreChange.compareTo(BigDecimal.ZERO) != 0) {
            String creditTitle = "信用分变动";
            String creditContent = "由于收到" + rating + "星评价，您的信用分"
                    + (scoreChange.compareTo(BigDecimal.ZERO) > 0 ? "+" : "")
                    + scoreChange + "（当前：" + userService.getUserById(toUserId).getCreditScore() + "）";
            notificationService.createNotification(toUserId, creditTitle, creditContent,
                    "CREDIT_CHANGE", null);
        }

        return evaluation;
    }

    public List<Evaluation> getEvaluationsForUser(Long toUserId) {
        return evaluationRepository.findByToUserUserId(toUserId);
    }

    public List<Evaluation> getEvaluationsForGroup(Long groupId) {
        return evaluationRepository.findByGroupGroupId(groupId);
    }

    public BigDecimal getAverageRating(Long userId) {
        BigDecimal avg = evaluationRepository.calculateAverageRating(userId);
        return avg != null ? avg : BigDecimal.ZERO;
    }
}
