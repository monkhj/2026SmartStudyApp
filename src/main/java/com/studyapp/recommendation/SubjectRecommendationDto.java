package com.studyapp.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * F11 - 과목별 추천 정보 DTO
 * Java record + Jackson 직렬화 호환
 *
 * record의 boolean 필드는 isXxx() → xxx()로 직렬화되므로
 * @JsonProperty("isWeak")로 명시해서 JS fetch 응답 필드명 보장
 */
public record SubjectRecommendationDto(
        String subjectName,
        double averageAccuracy,
        long quizCount,
        @JsonProperty("isWeak") boolean isWeak,
        String level
) {
    public static String resolveLevel(double accuracy) {
        if (accuracy < 60.0) return "취약";
        if (accuracy < 80.0) return "보통";
        return "우수";
    }

    public String colorClass() {
        return switch (level) {
            case "취약" -> "red";
            case "보통" -> "amber";
            default    -> "teal";
        };
    }
}
