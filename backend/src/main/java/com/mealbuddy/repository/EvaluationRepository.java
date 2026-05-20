package com.mealbuddy.repository;

import com.mealbuddy.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByGroupGroupId(Long groupId);
    List<Evaluation> findByToUserUserId(Long toUserId);

    @Query("SELECT AVG(e.rating) FROM Evaluation e WHERE e.toUser.userId = ?1")
    BigDecimal calculateAverageRating(Long userId);

    boolean existsByGroupGroupIdAndFromUserUserIdAndToUserUserId(Long groupId, Long fromUserId, Long toUserId);
}
