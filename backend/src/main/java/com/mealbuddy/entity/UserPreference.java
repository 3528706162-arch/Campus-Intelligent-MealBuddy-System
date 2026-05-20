package com.mealbuddy.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "user_preference")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "taste_tags", length = 500)
    private String tasteTags; // JSON string: ["川菜","粤菜","日料"]

    @Column(length = 500)
    private String taboo; // 忌口, JSON string

    @Column(name = "favorite_canteen", length = 200)
    private String favoriteCanteen;

    @Column(name = "meal_time_period", length = 100)
    private String mealTimePeriod; // JSON: ["11:30-12:30","17:30-18:30"]
}
