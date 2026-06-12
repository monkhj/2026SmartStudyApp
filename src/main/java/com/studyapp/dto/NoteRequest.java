package com.studyapp.dto;

public class NoteRequest {
    private String title;
    private String userId;
    private String subjectId;
    private String text;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
