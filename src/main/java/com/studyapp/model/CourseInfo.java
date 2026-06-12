package com.studyapp.model;

public class CourseInfo {
    private String id;
    private String universityName;
    private String department;
    private String subjectName;
    private String professor;
    private String room;
    private int credit;
    private String category;
    private int dayOfWeek;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private String color;

    // 기본 생성자
    public CourseInfo() {}

    // Getter / Setter 메서드 (다른 파일들과 매칭되는 핵심 통로)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getProfessor() { return professor; }
    public void setProfessor(String professor) { this.professor = professor; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public int getCredit() { return credit; }
    public void setCredit(int credit) { this.credit = credit; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getStartHour() { return startHour; }
    public void setStartHour(int startHour) { this.startHour = startHour; }

    public int getStartMinute() { return startMinute; }
    public void setStartMinute(int startMinute) { this.startMinute = startMinute; }

    public int getEndHour() { return endHour; }
    public void setEndHour(int endHour) { this.endHour = endHour; }

    public int getEndMinute() { return endMinute; }
    public void setEndMinute(int endMinute) { this.endMinute = endMinute; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}