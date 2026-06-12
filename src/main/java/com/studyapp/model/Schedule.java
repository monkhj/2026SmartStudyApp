package com.studyapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "schedules")
public class Schedule {
    @Id
    private String id;
    private String userId;
    private String subjectName;
    private String color;
    private int dayOfWeek;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private String memo;
    private String professor;
    private String room;
}
