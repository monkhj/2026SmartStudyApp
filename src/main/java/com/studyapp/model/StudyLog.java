package com.studyapp.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "study_logs")
@CompoundIndexes({
    @CompoundIndex(def = "{'userId': 1, 'date': 1}"),
    @CompoundIndex(def = "{'userId': 1, 'subjectId': 1, 'date': 1}")
})
public class StudyLog {

    @Id
    private String    id;
    private String    userId;
    private String    subjectId;
    private LocalDate date;
    private int       durationMinutes;
    private String    activityType; // "NOTE_UPLOAD", "QUIZ", "REVIEW"
}
