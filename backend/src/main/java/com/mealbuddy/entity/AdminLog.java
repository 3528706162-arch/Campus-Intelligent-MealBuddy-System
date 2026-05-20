package com.mealbuddy.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType; // BAN_USER, UNBAN_USER, DELETE_GROUP

    @Column(name = "target_type", length = 30)
    private String targetType; // USER, GROUP

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "operation_detail", length = 500)
    private String operationDetail;

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}
