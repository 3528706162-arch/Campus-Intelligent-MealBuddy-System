package com.mealbuddy.entity;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "credit_score", precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal creditScore = new BigDecimal("100.0");

    @Column(length = 20)
    @Builder.Default
    private String role = "USER"; // USER or ADMIN

    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1; // 1=正常, 0=封禁

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}
