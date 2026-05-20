package com.mealbuddy.config;

import com.mealbuddy.entity.GroupActivity;
import com.mealbuddy.repository.GroupActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTasks {

    private final GroupActivityRepository groupRepository;

    // 每小时执行一次：已过用餐时间的CONFIRMED组局 → FINISHED
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void autoFinishGroups() {
        List<GroupActivity> groups = groupRepository.findByStatusAndMealTimeBefore(
                "CONFIRMED", LocalDateTime.now());
        for (GroupActivity g : groups) {
            g.setStatus("FINISHED");
            groupRepository.save(g);
            log.info("Auto-finished group: {} ({})", g.getGroupId(), g.getTitle());
        }
        if (!groups.isEmpty()) {
            log.info("Auto-finished {} groups", groups.size());
        }
    }

    // 每小时执行一次：用餐时间超48h的FINISHED → 关闭评价
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void closeEvalWindow() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(48);
        List<GroupActivity> groups = groupRepository.findByStatusAndMealTimeBefore(
                "FINISHED", deadline);
        // 评价窗口已自然关闭，这里记录日志
        if (!groups.isEmpty()) {
            log.info("Evaluation window closed for {} groups", groups.size());
        }
    }
}
