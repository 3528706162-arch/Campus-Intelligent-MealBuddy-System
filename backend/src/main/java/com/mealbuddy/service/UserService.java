package com.mealbuddy.service;

import com.mealbuddy.entity.User;
import com.mealbuddy.entity.UserPreference;
import com.mealbuddy.exception.BusinessException;
import com.mealbuddy.repository.UserPreferenceRepository;
import com.mealbuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final NotificationService notificationService;

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("AUTH_001", "用户不存在", HttpStatus.NOT_FOUND));
    }

    public UserPreference getPreference(Long userId) {
        return preferenceRepository.findByUserUserId(userId)
                .orElse(null);
    }

    @Transactional
    public UserPreference updatePreference(Long userId, Map<String, String> pref) {
        User user = getUserById(userId);
        UserPreference preference = preferenceRepository.findByUserUserId(userId)
                .orElseGet(() -> UserPreference.builder().user(user).build());

        if (pref.containsKey("tasteTags")) preference.setTasteTags(pref.get("tasteTags"));
        if (pref.containsKey("taboo")) preference.setTaboo(pref.get("taboo"));
        if (pref.containsKey("favoriteCanteen")) preference.setFavoriteCanteen(pref.get("favoriteCanteen"));
        if (pref.containsKey("mealTimePeriod")) preference.setMealTimePeriod(pref.get("mealTimePeriod"));

        return preferenceRepository.save(preference);
    }

    @Transactional
    public void updateCreditScore(Long userId, BigDecimal delta) {
        User user = getUserById(userId);
        BigDecimal oldScore = user.getCreditScore();
        BigDecimal newScore = oldScore.add(delta);
        if (newScore.compareTo(BigDecimal.ZERO) < 0) newScore = BigDecimal.ZERO;
        if (newScore.compareTo(new BigDecimal("100")) > 0) newScore = new BigDecimal("100");
        user.setCreditScore(newScore);
        userRepository.save(user);

        // 信用分变动通知
        BigDecimal actualDelta = newScore.subtract(oldScore);
        if (actualDelta.compareTo(BigDecimal.ZERO) != 0) {
            String title = "信用分变动";
            String sign = actualDelta.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
            String content = "您的信用分 " + sign + actualDelta
                    + "（" + oldScore + " → " + newScore + "）";
            notificationService.createNotification(userId, title, content, "CREDIT_CHANGE");
        }
    }

    public boolean canOperate(Long userId, String operation) {
        User user = getUserById(userId);
        BigDecimal threshold = new BigDecimal("60");
        return user.getCreditScore().compareTo(threshold) >= 0;
    }

    @Transactional
    public User updateProfile(Long userId, String username, String phone) {
        User user = getUserById(userId);
        if (username != null && !username.isBlank() && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new BusinessException("BIZ_JOIN_003", "该用户名已被使用", HttpStatus.CONFLICT);
            }
            user.setUsername(username.trim());
        }
        if (phone != null) {
            user.setPhone(phone.trim());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = getUserById(userId);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }
}
