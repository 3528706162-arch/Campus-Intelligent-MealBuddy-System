package com.mealbuddy.service;

import com.mealbuddy.entity.Notification;
import com.mealbuddy.entity.User;
import com.mealbuddy.repository.NotificationRepository;
import com.mealbuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(Long userId, String title, String content, String msgType) {
        createNotification(userId, title, content, msgType, null);
    }

    @Transactional
    public void createNotification(Long userId, String title, String content, String msgType, Long relatedId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .msgType(msgType)
                .relatedId(relatedId)
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserUserIdOrderByCreateTimeDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserUserIdAndIsReadFalseOrderByCreateTimeDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void broadcastAnnouncement(String title, String content) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .content(content)
                    .msgType("ANNOUNCEMENT")
                    .build();
            notificationRepository.save(notification);
        }
    }
}
