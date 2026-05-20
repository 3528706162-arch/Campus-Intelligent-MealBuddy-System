package com.mealbuddy.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupActivity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "apply_time", nullable = false)
    @Builder.Default
    private LocalDateTime applyTime = LocalDateTime.now();

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";
    // PENDING(待审核), APPROVED(已通过), REJECTED(已拒绝), CANCELLED(已取消)

    @Column(length = 200)
    private String remark;
}
