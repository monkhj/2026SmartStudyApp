package com.studyapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyapp.model.CourseInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiCourseSearchService implements CourseSearchService {

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.model:llama-3.3-70b-versatile}")
    private String model;

    private static final String[] COLORS = {
        "#2563eb", "#7c3aed", "#db2777", "#dc2626",
        "#d97706", "#16a34a", "#0891b2", "#475569",
        "#9333ea", "#ea580c", "#0d9488", "#4f46e5"
    };

    @Override
    public List<CourseInfo> searchCourses(String university, String department) {
        String prompt = buildPrompt(university, department);

        // Groq는 OpenAI 호환 형식 사용
        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system",
                       "content", "당신은 한국 대학교 강의 정보 전문가입니다. 반드시 순수 JSON 배열만 반환하세요."),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.1
        );

        try {
            String response = geminiWebClient.post()
                    .uri("/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseResponse(response, university, department);

        } catch (WebClientResponseException e) {
            log.error("Groq API 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("강의 정보를 불러오는데 실패했습니다. (HTTP " + e.getStatusCode() + ")");
        } catch (Exception e) {
            log.error("Groq API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("강의 정보를 불러오는데 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private String buildPrompt(String university, String department) {
        return """
            %s %s의 2025년 2학기 수강 가능한 강의 목록을 JSON 배열로만 반환하세요.
            설명, 마크다운, 코드블록 없이 순수 JSON만 출력하세요.

            반드시 아래 형식을 따르세요:
            [
              {
                "subjectName": "자료구조",
                "professor": "미정",
                "room": "공학관 301호",
                "credit": 3,
                "category": "전공필수",
                "dayOfWeek": 1,
                "startHour": 9,
                "startMinute": 0,
                "endHour": 10,
                "endMinute": 30
              }
            ]

            규칙:
            - category는 "전공필수", "전공선택", "교양" 중 하나
            - dayOfWeek: 1=월요일, 2=화요일, 3=수요일, 4=목요일, 5=금요일
            - 전공필수 5개, 전공선택 8개, 교양 4개, 총 17개 이상 반환
            - 시간은 겹치지 않게 구성
            - 실제 해당 학과에서 개설될 법한 과목명 사용
            - 모든 필드 반드시 포함

            대학교: %s
            학과: %s
            """.formatted(university, department, university, department);
    }

    private List<CourseInfo> parseResponse(String rawResponse,
                                            String university, String department) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            // Groq 응답 구조: choices[0].message.content
            String text = root
                    .path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            // 마크다운 코드블록 제거 (안전장치)
            text = text.replaceAll("(?s)```json\\s*", "")
                       .replaceAll("(?s)```\\s*", "")
                       .trim();

            JsonNode coursesNode = objectMapper.readTree(text);
            List<CourseInfo> courses = new ArrayList<>();
            int colorIdx = 0;

            for (JsonNode node : coursesNode) {
                CourseInfo course = new CourseInfo();
                course.setId(UUID.randomUUID().toString());
                course.setUniversityName(university);
                course.setDepartment(department);
                course.setSubjectName(node.path("subjectName").asText());
                course.setProfessor(node.path("professor").asText());
                course.setRoom(node.path("room").asText());
                course.setCredit(node.path("credit").asInt(3));
                course.setCategory(node.path("category").asText("전공선택"));
                course.setDayOfWeek(node.path("dayOfWeek").asInt(1));
                course.setStartHour(node.path("startHour").asInt(9));
                course.setStartMinute(node.path("startMinute").asInt(0));
                course.setEndHour(node.path("endHour").asInt(10));
                course.setEndMinute(node.path("endMinute").asInt(30));
                course.setColor(COLORS[colorIdx++ % COLORS.length]);
                courses.add(course);
            }

            log.info("강의 검색 완료: {} {} → {}개", university, department, courses.size());
            return courses;

        } catch (Exception e) {
            log.error("Groq 응답 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("강의 데이터 파싱에 실패했습니다.");
        }
    }
}