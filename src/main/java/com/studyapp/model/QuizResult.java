package com.studyapp.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quiz_results")
@CompoundIndexes({
    @CompoundIndex(def = "{'userId': 1, 'subjectId': 1}"),
    @CompoundIndex(def = "{'userId': 1, 'correct': 1}")
})
public class QuizResult {

    @Id
    private String        id;
    private String        userId;
    private String        subjectId;
    private String        questionText;
    private String        userAnswer;
    private String        correctAnswer;
    private String        explanation;
    private boolean       correct;
    private int           totalCount;
    private int           correctCount;
    private LocalDateTime solvedAt;
}
