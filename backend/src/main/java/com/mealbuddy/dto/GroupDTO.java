package com.mealbuddy.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GroupDTO {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "标题不能为空")
        private String title;
        @NotBlank(message = "食堂不能为空")
        private String canteen;
        @NotNull(message = "用餐时间不能为空")
        private LocalDateTime mealTime;
        @Min(value = 2, message = "最少2人")
        @Max(value = 20, message = "最多20人")
        private Integer maxPeople;
        private String remark;
    }

    @Data
    public static class GroupResponse {
        private Long groupId;
        private String title;
        private String canteen;
        private LocalDateTime mealTime;
        private Integer maxPeople;
        private Integer currentPeople;
        private String status;
        private String remark;
        private String initiatorName;
        private Long initiatorId;
        private LocalDateTime createTime;
    }

    @Data
    public static class ApplicationRequest {
        private String remark;
    }
}
