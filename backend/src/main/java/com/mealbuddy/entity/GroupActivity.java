package com.mealbuddy.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_activity")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class GroupActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 100)
    private String canteen;

    @Column(name = "meal_time", nullable = false)
    private LocalDateTime mealTime;

    @Column(name = "max_people", nullable = false)
    private Integer maxPeople;

    @Column(name = "current_people")
    @Builder.Default
    private Integer currentPeople = 1;

    @Column(length = 20)
    @Builder.Default
    private String status = "RECRUITING";
    // RECRUITING(招募中), CONFIRMED(已组队), FINISHED(已完成), CANCELLED(已取消)

    @Column(length = 500)
    private String remark;

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}
