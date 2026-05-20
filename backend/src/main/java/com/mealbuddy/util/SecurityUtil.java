package com.mealbuddy.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }

    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            return auth.getAuthorities().stream()
                    .map(Object::toString)
                    .filter(r -> r.contains("ADMIN") || r.contains("USER"))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return role != null && role.contains("ADMIN");
    }
}
