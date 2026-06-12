package com.studyapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "notification_logs")
public class NotificationLog {
    @Id
    private String id;
    private String userId;
    private String type; // "REVIEW" 또는 "DDAY_7", "DDAY_3" 등
    private String targetId; // ReviewSchedule ID 또는 Event ID
    private String title;
    private LocalDateTime sentAt;
    private String status; // "SUCCESS" 또는 "FAIL"
    private String errorMessage;
}