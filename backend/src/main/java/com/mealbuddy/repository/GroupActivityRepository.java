package com.mealbuddy.repository;

import com.mealbuddy.entity.GroupActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GroupActivityRepository extends JpaRepository<GroupActivity, Long> {

    Page<GroupActivity> findByStatusOrderByCreateTimeDesc(String status, Pageable pageable);

    Page<GroupActivity> findByStatusAndCanteenContainingOrderByCreateTimeDesc(String status, String canteen, Pageable pageable);

    List<GroupActivity> findByInitiatorUserId(Long userId);

    @Query("SELECT g FROM GroupActivity g WHERE g.status IN ('RECRUITING', 'CONFIRMED') ORDER BY g.createTime DESC")
    Page<GroupActivity> findActiveGroups(Pageable pageable);

    List<GroupActivity> findByStatusAndMealTimeBefore(String status, java.time.LocalDateTime time);
}
