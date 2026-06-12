package com.studyapp.dto;

import java.time.LocalDateTime;

// Java 17 record — setter 불필요한 순수 응답 DTO
public record WrongAnswerItem(
    String        id,
    String        subjectId,
    String        subjectName,
    String        questionText,
    String        userAnswer,
    String        correctAnswer,
    String        explanation,
    LocalDateTime solvedAt
) {}
