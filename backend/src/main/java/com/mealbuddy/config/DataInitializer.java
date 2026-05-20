package com.mealbuddy.config;

import com.mealbuddy.entity.*;
import com.mealbuddy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GroupActivityRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // 创建管理员
        User admin = User.builder()
                .username("管理员")
                .email("admin@mealbuddy.com")
                .password(passwordEncoder.encode("admin123"))
                .phone("13800000000")
                .role("ADMIN")
                .creditScore(new BigDecimal("100.0"))
                .build();
        admin = userRepository.save(admin);

        // 创建测试用户
        User user1 = User.builder()
                .username("张三")
                .email("zhangsan@test.com")
                .password(passwordEncoder.encode("123456"))
                .phone("13800000001")
                .role("USER")
                .creditScore(new BigDecimal("100.0"))
                .build();
        user1 = userRepository.save(user1);

        User user2 = User.builder()
                .username("李四")
                .email("lisi@test.com")
                .password(passwordEncoder.encode("123456"))
                .phone("13800000002")
                .role("USER")
                .creditScore(new BigDecimal("85.0"))
                .build();
        user2 = userRepository.save(user2);

        User user3 = User.builder()
                .username("王五")
                .email("wangwu@test.com")
                .password(passwordEncoder.encode("123456"))
                .phone("13800000003")
                .role("USER")
                .creditScore(new BigDecimal("90.0"))
                .build();
        userRepository.save(user3);

        // 创建示例约饭组
        GroupActivity group1 = GroupActivity.builder()
                .initiator(user1)
                .title("午饭搭子，第一食堂走起")
                .canteen("第一食堂")
                .mealTime(LocalDateTime.now().plusHours(2))
                .maxPeople(4)
                .currentPeople(2)
                .remark("想吃宫保鸡丁，有一起的吗？")
                .build();
        groupRepository.save(group1);

        GroupActivity group2 = GroupActivity.builder()
                .initiator(user2)
                .title("晚餐约风味餐厅")
                .canteen("风味餐厅")
                .mealTime(LocalDateTime.now().plusHours(5))
                .maxPeople(3)
                .currentPeople(1)
                .remark("听说风味餐厅新出了兰州拉面")
                .build();
        groupRepository.save(group2);

        GroupActivity group3 = GroupActivity.builder()
                .initiator(user1)
                .title("周末聚餐，第二食堂")
                .canteen("第二食堂")
                .mealTime(LocalDateTime.now().plusDays(3))
                .maxPeople(6)
                .currentPeople(3)
                .status("CONFIRMED")
                .remark("周末有空的一起")
                .build();
        groupRepository.save(group3);
    }
}
