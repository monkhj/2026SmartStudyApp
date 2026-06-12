package com.studyapp.recommendation;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recommendation_quiz_results")
public class RecommendationQuizResult {
    @Id
    private String id;
    private String userId;
    private String subjectName;
    private double accuracyRate;
    private int totalQuestions;
    private int correctAnswers;
}
