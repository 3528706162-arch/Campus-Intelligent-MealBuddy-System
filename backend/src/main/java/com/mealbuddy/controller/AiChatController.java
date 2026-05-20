package com.mealbuddy.controller;

import com.mealbuddy.dto.Result;
import com.mealbuddy.entity.AiChatRecord;
import com.mealbuddy.service.AiChatService;
import com.mealbuddy.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        String question = body.get("question");
        if (question == null || question.isBlank()) {
            return Result.fail("问题不能为空");
        }
        return Result.ok(aiChatService.chat(userId, question));
    }

    @PostMapping("/polish")
    public Result<?> polish(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        String canteen = body.getOrDefault("canteen", "");
        String mealTime = body.getOrDefault("mealTime", "");
        String remark = body.getOrDefault("remark", "");
        String prompt = "请为以下约饭活动写一段吸引人的招募文案（50-100字，活泼风格，像一个大二学生在邀请饭友）：\n"
                + "食堂：" + canteen + "\n"
                + "时间：" + mealTime + "\n"
                + (remark.isEmpty() ? "" : "备注：" + remark + "\n")
                + "直接输出文案内容，不要加引号或其他说明。";
        return Result.ok(aiChatService.chat(userId, prompt));
    }

    @GetMapping("/history")
    public Result<List<AiChatRecord>> history() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok(aiChatService.getHistory(userId));
    }
}
