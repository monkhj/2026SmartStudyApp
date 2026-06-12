package com.studyapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyapp.model.StudyLog;
import com.studyapp.repository.StudyLogRepository;
import com.studyapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/study-log")
@RequiredArgsConstructor
public class StudyLogController {

    private static final Logger log = Logger.getLogger("StudyLogController");

    private final StudyLogRepository studyLogRepository;
    private final UserService        userService;
    private final ObjectMapper       objectMapper;

    @PostMapping("/save-subject")
    public ResponseEntity<?> saveSubjectStudyLog(
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            String body = sb.toString().trim();
            if (body.isEmpty()) return ResponseEntity.ok(Map.of("saved", false));

            Map<String, Object> map = objectMapper.readValue(body, Map.class);
            Object minVal = map.getOrDefault("minutes", 0);
            int minutes = minVal instanceof Number ? ((Number) minVal).intValue() : 0;
            String subjectId = (String) map.getOrDefault("subjectId", "");
            if (minutes <= 0 || subjectId.isBlank())
                return ResponseEntity.ok(Map.of("saved", false));

            String username = null;
            if (userDetails != null) {
                username = userDetails.getUsername();
            } else {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()
                        && !auth.getPrincipal().equals("anonymousUser"))
                    username = auth.getName();
            }
            if (username == null) return ResponseEntity.status(401).build();

            String userId = userService.findByUsername(username).getId();
            LocalDate today = LocalDate.now();

            // 과목별 로그에 누적
            List<StudyLog> todayLogs = studyLogRepository
                    .findByUserIdAndSubjectIdAndDateAfter(userId, subjectId, today.minusDays(1))
                    .stream()
                    .filter(l -> l.getDate().equals(today))
                    .collect(java.util.stream.Collectors.toList());

            if (!todayLogs.isEmpty()) {
                StudyLog sl = todayLogs.get(0);
                sl.setDurationMinutes(sl.getDurationMinutes() + minutes);
                studyLogRepository.save(sl);
            } else {
                StudyLog sl = StudyLog.builder()
                        .userId(userId)
                        .subjectId(subjectId)
                        .date(today)
                        .durationMinutes(minutes)
                        .activityType("NOTE_VIEW")
                        .build();
                studyLogRepository.save(sl);
            }

            log.info("[StudyLog] 과목별 " + subjectId + " " + minutes + "분 userId=" + userId);
            return ResponseEntity.ok(Map.of("saved", true));

        } catch (Exception e) {
            log.warning("[StudyLog] 과목별 저장 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveStudyLog(
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            // body 읽기 (sendBeacon은 text/plain으로 올 수 있음)
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            String body = sb.toString().trim();
            if (body.isEmpty()) return ResponseEntity.ok(Map.of("saved", false));

            Map<String, Object> map = objectMapper.readValue(body, Map.class);
            Object minVal = map.getOrDefault("minutes", 0);
            int minutes = minVal instanceof Number ? ((Number) minVal).intValue() : 0;
            if (minutes <= 0) return ResponseEntity.ok(Map.of("saved", false));

            // 유저 확인
            String username = null;
            if (userDetails != null) {
                username = userDetails.getUsername();
            } else {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()
                        && !auth.getPrincipal().equals("anonymousUser")) {
                    username = auth.getName();
                }
            }
            if (username == null) {
                log.warning("[StudyLog] 인증 없음 - 저장 스킵");
                return ResponseEntity.status(401).build();
            }

            String userId = userService.findByUsername(username).getId();
            LocalDate today = LocalDate.now();

            // SESSION 타입만 찾아서 누적 (NOTE_VIEW와 분리)
            List<StudyLog> todayLogs = studyLogRepository.findByUserIdAndDate(userId, today)
                    .stream()
                    .filter(l -> "SESSION".equals(l.getActivityType()))
                    .collect(java.util.stream.Collectors.toList());
            if (!todayLogs.isEmpty()) {
                StudyLog sl = todayLogs.get(0);
                sl.setDurationMinutes(sl.getDurationMinutes() + minutes);
                studyLogRepository.save(sl);
                log.info("[StudyLog] 누적 " + sl.getDurationMinutes() + "분 userId=" + userId);
            } else {
                StudyLog sl = StudyLog.builder()
                        .userId(userId)
                        .date(today)
                        .durationMinutes(minutes)
                        .activityType("SESSION")
                        .build();
                studyLogRepository.save(sl);
                log.info("[StudyLog] 신규 " + minutes + "분 userId=" + userId);
            }

            return ResponseEntity.ok(Map.of("saved", true, "minutes", minutes));

        } catch (Exception e) {
            log.warning("[StudyLog] 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}