package com.mealbuddy.repository;

import com.mealbuddy.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserUserIdOrderByCreateTimeDesc(Long userId);
    List<Notification> findByUserUserIdAndIsReadFalseOrderByCreateTimeDesc(Long userId);
    long countByUserUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = ?1")
    void markAllAsRead(Long userId);
}
