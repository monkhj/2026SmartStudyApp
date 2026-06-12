package com.studyapp.controller;

import com.studyapp.service.GeminiCourseSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseSearchController {

    private static final Logger log = Logger.getLogger(CourseSearchController.class.getName());
    private final GeminiCourseSearchService geminiCourseSearchService;

    // GET 방식 (기존)
    @GetMapping("/search")
    public ResponseEntity<?> searchCoursesGet(
            @RequestParam String university,
            @RequestParam(required = false, defaultValue = "") String department) {
        return doSearch(university, department);
    }

    // POST 방식 (HTML에서 호출)
    @PostMapping("/search")
    public ResponseEntity<?> searchCoursesPost(@RequestBody Map<String, String> body) {
        String university = body.getOrDefault("university", "");
        String department = body.getOrDefault("department", "");
        return doSearch(university, department);
    }

    private ResponseEntity<?> doSearch(String university, String department) {
        try {
            log.info("[CourseSearch] 검색: " + university + " " + department);
            return ResponseEntity.ok(geminiCourseSearchService.searchCourses(university, department));
        } catch (Exception e) {
            log.warning("[CourseSearch] 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
