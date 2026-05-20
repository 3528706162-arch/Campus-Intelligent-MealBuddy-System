package com.mealbuddy.dto;

import lombok.Data;
import javax.validation.constraints.*;

public class EvaluationDTO {

    @Data
    public static class SubmitRequest {
        @NotNull
        private Long toUserId;
        @NotNull
        @DecimalMin("1.0") @DecimalMax("5.0")
        private Double rating;
        @Size(max = 500)
        private String content;
        private Boolean anonymous;
    }
}
