package com.mealbuddy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealbuddy.entity.AiChatRecord;
import com.mealbuddy.entity.User;
import com.mealbuddy.entity.UserPreference;
import com.mealbuddy.repository.AiChatRecordRepository;
import com.mealbuddy.repository.UserPreferenceRepository;
import com.mealbuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiChatRecordRepository aiChatRecordRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.api-url}")
    private String apiUrl;

    @Value("${app.ai.model:deepseek-chat}")
    private String model;

    @Value("${app.ai.timeout:10000}")
    private int timeout;

    @Value("${app.ai.fallback-enabled:true}")
    private boolean fallbackEnabled;

    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT =
        "你是上海理工大学「校园饭搭子」系统的AI美食助手。你熟悉校内所有食堂和招牌菜品。" +
        "你能根据学生的口味偏好、所在校区、预算等给出个性化用餐建议。" +
        "你可以帮学生推荐食堂、介绍菜品、提供约饭建议。" +
        "回答要简洁、友好、活泼，像一个大二学长/学姐在推荐好吃的。" +
        "相关的食堂信息：\n" +
        "北校区(军工路516号)：第一食堂(川湘菜,特色酱鸭,蛋糕),第二食堂(本帮菜,肉糜蒸蛋,虎皮尖椒,油爆虾,红烧肉)," +
        "第五食堂(战斧猪排,减脂餐16-18元),阅餐厅(仅晚餐),咪昵餐厅(盖浇饭,面食,米粉)\n" +
        "南校区(军工路334号)：思餐厅一楼(600+种美食,香汁肥牛拌饭,三鲜丸子拌饭),思餐厅二楼西餐(沙拉,牛排,意面)," +
        "思餐厅二楼民族餐厅(肉夹馍,烤羊排,羊肉抓饭,大盘鸡),思餐厅三楼风味餐厅(麻辣烫,酸辣粉,咖喱饭,汉堡)\n" +
        "军工路1100号：第四食堂(本帮菜,川湘菜,时令菜),民族餐厅(清真,西北面点)\n" +
        "复兴路校区：复兴路食堂(本帮菜,大众餐)";

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        restTemplate = new RestTemplate(factory);
    }

    @Transactional
    public Map<String, Object> chat(Long userId, String question) {
        User user = userRepository.findById(userId).orElseThrow();
        UserPreference prefs = preferenceRepository.findByUserUserId(userId).orElse(null);

        long startTime = System.currentTimeMillis();
        String reply;
        boolean usedFallback = false;

        try {
            reply = callDeepSeekApi(user, prefs, question);
            log.info("DeepSeek API responded in {}ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.warn("DeepSeek API call failed ({} - {}), falling back to local template",
                    e.getClass().getSimpleName(), e.getMessage());
            if (fallbackEnabled) {
                reply = generateLocalReply(user, prefs, question);
                usedFallback = true;
            } else {
                reply = "抱歉，AI服务暂时不可用，请稍后再试。";
            }
        }

        AiChatRecord record = AiChatRecord.builder()
                .user(user)
                .userQuestion(question)
                .aiReply(reply)
                .build();
        aiChatRecordRepository.save(record);

        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("reply", reply);
        result.put("fallback", usedFallback);
        return result;
    }

    private String callDeepSeekApi(User user, UserPreference prefs, String question) throws Exception {
        // Build user context
        StringBuilder userContext = new StringBuilder();
        userContext.append("当前用户信息：用户名=").append(user.getUsername())
                .append("，信用分=").append(user.getCreditScore());
        if (prefs != null) {
            if (prefs.getTasteTags() != null && !prefs.getTasteTags().isEmpty()) {
                userContext.append("，口味偏好=").append(prefs.getTasteTags());
            }
            if (prefs.getFavoriteCanteen() != null && !prefs.getFavoriteCanteen().isEmpty()) {
                userContext.append("，常去食堂=").append(prefs.getFavoriteCanteen());
            }
            if (prefs.getTaboo() != null && !prefs.getTaboo().isEmpty()) {
                userContext.append("，忌口=").append(prefs.getTaboo());
            }
        }

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 800);
        requestBody.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        Map<String, String> contextMsg = new HashMap<>();
        contextMsg.put("role", "system");
        contextMsg.put("content", userContext.toString());
        messages.add(contextMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", question);
        messages.add(userMsg);

        requestBody.put("messages", messages);

        // Build HTTP request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String bodyJson = objectMapper.writeValueAsString(requestBody);
        HttpEntity<String> request = new HttpEntity<>(bodyJson, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null && message.get("content") != null) {
                    return message.get("content").toString().trim();
                }
            }
        }

        throw new RuntimeException("Unexpected API response: " + response.getStatusCode());
    }

    private String generateLocalReply(User user, UserPreference prefs, String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("（当前为离线模式，AI助手正在使用本地知识库）\n\n");
        String q = question.toLowerCase();

        if (q.contains("推荐") || q.contains("吃什么") || q.contains("食堂") || q.contains("美食")) {
            sb.append("🍽️ 上海理工大学食堂智能推荐：\n\n");
            sb.append("【军工路516号·北校区】\n");
            sb.append("1. 第一食堂 — 川湘菜系，特色酱鸭、网红蛋糕\n");
            sb.append("2. 第二食堂 — 本帮菜，王牌：肉糜蒸蛋、虎皮尖椒、油爆虾\n");
            sb.append("3. 第五食堂 — 战斧猪排、减脂餐(大虾/牛肉/鸡胸肉 16-18元)\n\n");
            sb.append("【军工路334号·南校区】\n");
            sb.append("4. 思餐厅一楼 — 香汁肥牛拌饭、三鲜丸子拌饭\n");
            sb.append("5. 思餐厅二楼西餐厅 — 沙拉、牛排、意大利面\n");
            sb.append("6. 思餐厅二楼民族餐厅 — 肉夹馍、烤羊排、羊肉抓饭\n");
            sb.append("7. 思餐厅三楼风味餐厅 — 麻辣烫、酸辣粉、咖喱饭\n\n");

            if (prefs != null && prefs.getTasteTags() != null && !prefs.getTasteTags().isEmpty()) {
                sb.append("根据您偏好的 ").append(prefs.getTasteTags()).append("，为您精准推荐：\n");
                if (prefs.getTasteTags().contains("川湘菜") || prefs.getTasteTags().contains("麻辣烫")) {
                    sb.append("→ 第一食堂（川湘菜系）或 思餐厅三楼风味餐厅\n");
                }
                if (prefs.getTasteTags().contains("面食") || prefs.getTasteTags().contains("小吃")) {
                    sb.append("→ 思餐厅三楼风味餐厅 或 咪昵餐厅\n");
                }
                if (prefs.getTasteTags().contains("本帮菜") || prefs.getTasteTags().contains("减脂轻食")) {
                    sb.append("→ 第二食堂（本帮菜）或 第五食堂（减脂餐）\n");
                }
                if (prefs.getTasteTags().contains("清真") || prefs.getTasteTags().contains("西北菜") || prefs.getTasteTags().contains("烧烤")) {
                    sb.append("→ 思餐厅二楼民族餐厅（西北风味）\n");
                }
            }
            if (prefs != null && prefs.getFavoriteCanteen() != null && !prefs.getFavoriteCanteen().isEmpty()) {
                sb.append("\n您常去的 ").append(prefs.getFavoriteCanteen()).append(" 也是不错的选择！\n");
            }
            sb.append("\n需要帮您发起约饭组吗？");
        } else if (q.contains("北校区") || q.contains("516")) {
            sb.append("🏫 军工路516号（北校区）食堂：\n\n");
            sb.append("1. 第一食堂 — 川湘菜系+面食+烘焙甜点\n");
            sb.append("2. 第二食堂 — 本帮菜+炖菜，王牌：肉糜蒸蛋、虎皮尖椒\n");
            sb.append("3. 第五食堂 — 战斧猪排+减脂餐，健身首选\n");
            sb.append("4. 阅餐厅 — 多功能餐厅，仅晚餐开放\n");
            sb.append("5. 咪昵餐厅 — 盖浇饭、面食、米粉、套餐");
        } else if (q.contains("南校区") || q.contains("334")) {
            sb.append("🏫 军工路334号（南校区）食堂：\n\n");
            sb.append("1. 思餐厅一楼大众餐厅 — 600+种美食，香汁肥牛拌饭\n");
            sb.append("2. 思餐厅二楼西餐厅 — 沙拉、牛排、意大利面\n");
            sb.append("3. 思餐厅二楼民族餐厅 — 肉夹馍、烤羊排、大盘鸡\n");
            sb.append("4. 思餐厅三楼风味餐厅 — 麻辣烫、酸辣粉、咖喱饭");
        } else if (q.contains("减脂") || q.contains("减肥") || q.contains("健康")) {
            sb.append("🥗 健康减脂推荐：\n\n");
            sb.append("首推 **第五食堂** 健康减脂餐：\n");
            sb.append("- 大虾减脂餐 16元\n");
            sb.append("- 牛肉减脂餐 18元\n");
            sb.append("- 鸡胸肉减脂餐 16元\n\n");
            sb.append("也可选择 **思餐厅二楼西餐厅** 的沙拉、轻食。");
        } else if (q.contains("信用") || q.contains("分数") || q.contains("评分")) {
            sb.append("您的当前信用分：").append(user.getCreditScore()).append(" 分\n\n");
            sb.append("信用分规则：\n");
            sb.append("- 初始分数：100分\n");
            sb.append("- 临近用餐取消：-20分\n");
            sb.append("- 获得好评(4星以上)：+2分\n");
            sb.append("- 获得差评(2星以下)：-3分\n");
            sb.append("- 信用分低于60分将受限");
        } else if (q.contains("怎么") || q.contains("如何") || q.contains("帮助")) {
            sb.append("校园饭搭子系统使用指南：\n\n");
            sb.append("1. 完善个人口味偏好，获得精准推荐\n");
            sb.append("2. 浏览约饭广场，找到感兴趣的饭局\n");
            sb.append("3. 申请加入或自己发起约饭\n");
            sb.append("4. 用餐后互相评价，积累信用\n\n");
            sb.append("有任何关于食堂或使用的问题都可以问我哦！");
        } else {
            sb.append("您好！我是校园饭搭子AI助手。\n\n");
            sb.append("我可以帮您：\n");
            sb.append("- 🍜 推荐食堂和招牌美食\n");
            sb.append("- 🏫 查询各校区食堂信息\n");
            sb.append("- 🥗 推荐减脂健康餐\n");
            sb.append("- ⭐ 查询信用分和规则\n");
            sb.append("- 📖 解答使用问题\n\n");
            sb.append("请告诉我您需要什么帮助？");
        }

        return sb.toString();
    }

    public List<AiChatRecord> getHistory(Long userId) {
        return aiChatRecordRepository.findByUserUserIdOrderByCreateTimeDesc(userId);
    }
}
