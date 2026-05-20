package com.mealbuddy.repository;

import com.mealbuddy.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByGroupGroupId(Long groupId);
    List<Application> findByUserUserId(Long userId);
    Optional<Application> findByGroupGroupIdAndUserUserId(Long groupId, Long userId);
    List<Application> findByGroupGroupIdAndStatus(Long groupId, String status);
    List<Application> findByUserUserIdAndStatus(Long userId, String status);
    boolean existsByGroupGroupIdAndUserUserIdAndStatus(Long groupId, Long userId, String status);
}
