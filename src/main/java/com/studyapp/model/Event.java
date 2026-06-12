package com.studyapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "events")
public class Event {
    @Id
    private String id;
    private String userId;
    private String title;
    private String type;
    private String subjectName;
    private LocalDate dueDate;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean notificationEnabled = true; // 기본적으로 알림 활성화       
    private List<Integer> notifyBefore = new ArrayList<>(List.of(7, 3, 1)); // 기본 알림 (7일 전, 3일 전, 1일 전)

}
