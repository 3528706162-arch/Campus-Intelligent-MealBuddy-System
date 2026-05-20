package com.mealbuddy.controller;

import com.mealbuddy.dto.EvaluationDTO.*;
import com.mealbuddy.dto.Result;
import com.mealbuddy.entity.Evaluation;
import com.mealbuddy.service.EvaluationService;
import com.mealbuddy.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/{groupId}")
    public Result<?> submit(@PathVariable Long groupId, @Valid @RequestBody SubmitRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        Evaluation ev = evaluationService.submitEvaluation(
                userId, groupId, request.getToUserId(),
                BigDecimal.valueOf(request.getRating()), request.getContent(),
                request.getAnonymous());
        // 构造包含用户名的响应
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("evaluationId", ev.getEvaluationId());
        result.put("fromUsername", ev.getFromUser().getUsername());
        result.put("toUsername", ev.getToUser().getUsername());
        result.put("rating", ev.getRating());
        result.put("content", ev.getContent());
        result.put("anonymous", ev.getAnonymous());
        result.put("createTime", ev.getCreateTime());
        return Result.ok("评价成功", result);
    }

    @GetMapping("/user/{userId}")
    public Result<List<Evaluation>> getUserEvaluations(@PathVariable Long userId) {
        return Result.ok(evaluationService.getEvaluationsForUser(userId));
    }

    @GetMapping("/user/{userId}/rating")
    public Result<BigDecimal> getUserRating(@PathVariable Long userId) {
        return Result.ok(evaluationService.getAverageRating(userId));
    }

    @GetMapping("/group/{groupId}")
    public Result<?> getGroupEvaluations(@PathVariable Long groupId) {
        List<Evaluation> evals = evaluationService.getEvaluationsForGroup(groupId);
        List<java.util.Map<String, Object>> result = evals.stream().map(e -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("evaluationId", e.getEvaluationId());
            m.put("fromUserId", e.getFromUser().getUserId());
            m.put("fromUsername", e.getFromUser().getUsername());
            m.put("toUserId", e.getToUser().getUserId());
            m.put("toUsername", e.getToUser().getUsername());
            m.put("rating", e.getRating());
            m.put("content", e.getContent());
            m.put("anonymous", e.getAnonymous());
            m.put("createTime", e.getCreateTime());
            return m;
        }).toList();
        return Result.ok(result);
    }
}
