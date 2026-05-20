package com.mealbuddy.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "msg_type", length = 30)
    private String msgType;
    // APPLICATION, APPROVAL, REJECTION, EVALUATION, CREDIT_CHANGE, ANNOUNCEMENT

    @Column(name = "related_id")
    private Long relatedId; // groupId 或 evaluationId，用于跳转

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}
