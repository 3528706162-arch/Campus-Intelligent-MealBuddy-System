package com.mealbuddy.repository;

import com.mealbuddy.entity.AiChatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiChatRecordRepository extends JpaRepository<AiChatRecord, Long> {
    List<AiChatRecord> findByUserUserIdOrderByCreateTimeDesc(Long userId);
    void deleteByUserUserId(Long userId);
}
