package com.studyapp.dto;

import java.util.List;

public class QuizResultRequest {

    private List<Integer> wrongIndices;
    private List<QuizAnswerItem> answers;

    public List<Integer> getWrongIndices() { return wrongIndices; }
    public void setWrongIndices(List<Integer> v) { this.wrongIndices = v; }
    public List<QuizAnswerItem> getAnswers() { return answers; }
    public void setAnswers(List<QuizAnswerItem> v) { this.answers = v; }

    public static class QuizAnswerItem {
        private String questionText;
        private String userAnswer;
        private String correctAnswer;
        private String explanation;  // AI 해설
        private boolean correct;

        public String getQuestionText()  { return questionText; }
        public void setQuestionText(String v)  { this.questionText = v; }
        public String getUserAnswer()    { return userAnswer; }
        public void setUserAnswer(String v)    { this.userAnswer = v; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String v) { this.correctAnswer = v; }
        public String getExplanation()   { return explanation; }
        public void setExplanation(String v)   { this.explanation = v; }
        public boolean isCorrect()       { return correct; }
        public void setCorrect(boolean v)      { this.correct = v; }
    }
}
