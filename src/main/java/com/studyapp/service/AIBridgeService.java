package com.studyapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyapp.model.Note;
import com.studyapp.model.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AIBridgeService {

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL    = "llama-3.3-70b-versatile";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PROMPT_TEMPLATE = """
            아래 텍스트를 분석하여 반드시 다음 JSON 형식만 반환하세요.
            마크다운 코드블록 없이 순수 JSON만 출력하세요.
            모든 문자열은 한국어로 작성하세요.

            {
              "summary": "3~5문장으로 핵심 내용 요약",
              "keywords": ["핵심단어1", "핵심단어2", "핵심단어3", "핵심단어4", "핵심단어5"],
              "questions": [
                { "type": "ox", "question": "O/X 문제 내용", "answer": "O", "explanation": "정답 한 문장 설명" },
                { "type": "ox", "question": "O/X 문제 내용", "answer": "X", "explanation": "정답 한 문장 설명" },
                { "type": "short_answer", "question": "단답형 질문", "answer": "정답", "explanation": "정답 한 문장 설명" },
                { "type": "short_answer", "question": "단답형 질문", "answer": "정답", "explanation": "정답 한 문장 설명" },
                { "type": "short_answer", "question": "단답형 질문", "answer": "정답", "explanation": "정답 한 문장 설명" }
              ]
            }

            분석할 텍스트:
            """;

    public Note analyze(String userId, String subjectId, String title, String text) {
        String rawJson = callGroqApi(text);
        return parseResponse(userId, subjectId, title, text, rawJson);
    }

    private String callGroqApi(String text) {
        try {
            String prompt = PROMPT_TEMPLATE + text;

            String requestBody = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
                put("model", MODEL);
                put("messages", List.of(new java.util.HashMap<>() {{
                    put("role", "user");
                    put("content", prompt);
                }}));
                put("temperature", 0.3);
                put("max_tokens", 3000);
            }});

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(GROQ_URL, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.at("/choices/0/message/content").asText();

        } catch (Exception e) {
            throw new RuntimeException("Groq API 호출 실패: " + e.getMessage(), e);
        }
    }

    private Note parseResponse(String userId, String subjectId, String title,
                                String originalText, String rawJson) {
        String json = rawJson.strip()
                .replaceAll("^```[a-z]*\\n?", "")
                .replaceAll("```$", "")
                .strip();
        try {
            JsonNode root = objectMapper.readTree(json);
            Note note = new Note();
            note.setTitle(title);
            note.setUserId(userId);
            note.setSubjectId(subjectId);
            note.setOriginalText(originalText);
            note.setSummary(root.get("summary").asText());
            note.setCreatedAt(LocalDateTime.now());

            List<String> keywords = new ArrayList<>();
            root.get("keywords").forEach(kw -> keywords.add(kw.asText()));
            note.setKeywords(keywords);

            List<Question> questions = new ArrayList<>();
            root.get("questions").forEach(qNode -> {
                Question q = new Question();
                q.setType(qNode.get("type").asText());
                q.setQuestion(qNode.get("question").asText());
                q.setAnswer(qNode.get("answer").asText());
                // explanation 파싱 (없으면 기본값)
                JsonNode expl = qNode.get("explanation");
                q.setExplanation(expl != null && !expl.isNull()
                        ? expl.asText()
                        : "정답: " + qNode.get("answer").asText());
                questions.add(q);
            });
            note.setQuestions(questions);
            return note;

        } catch (Exception e) {
            throw new RuntimeException("AI 응답 JSON 파싱 실패: " + json, e);
        }
    }
}