package com.studyapp.recommendation;

import com.studyapp.recommendation.SubjectRecommendationDto;
import com.studyapp.recommendation.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.studyapp.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * F11 - 학습 추천 컨트롤러
 *
 * [GET]  /recommendation          → Thymeleaf 뷰 렌더링 (recommendation/index.html)
 * [GET]  /recommendation/api/all  → 전체 과목 통계 JSON (REST)
 * [GET]  /recommendation/api/weak → 취약 과목만 JSON (REST / 사이드바 뱃지 갱신용)
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    // ──────────────────────────────────────────────
    //  Thymeleaf 뷰 렌더링
    // ──────────────────────────────────────────────

    /**
     * GET /recommendation
     * 학습 추천 페이지 전체 렌더링
     * - allSubjects   : 과목별 정답률 리스트 (bar 차트용)
     * - weakSubjects  : 취약 과목 리스트 (넛지 배너용)
     * - summary       : 4개 stat 카드용 요약 데이터
     * - hasWeak       : 배너 표시 여부
     */
    private String resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return "guest";
        try {
            return userService.findByUsername(userDetails.getUsername()).getId();
        } catch (Exception e) {
            return userDetails.getUsername();
        }
    }

    @GetMapping
    public String recommendationPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String userId = resolveUserId(userDetails);
        if (userId == null) return "redirect:/auth/login";

        List<SubjectRecommendationDto> allSubjects;
        List<SubjectRecommendationDto> weakSubjects;
        RecommendationService.SummaryStats summary;

        try {
            allSubjects  = recommendationService.getAllSubjectStats(userId);
            weakSubjects = recommendationService.getWeakSubjects(userId);
            summary      = recommendationService.getSummaryStats(userId);
        } catch (Exception e) {
            allSubjects  = java.util.List.of();
            weakSubjects = java.util.List.of();
            summary      = new RecommendationService.SummaryStats(0, 0, 0.0, 0);
        }

        model.addAttribute("allSubjects",  allSubjects);
        model.addAttribute("weakSubjects", weakSubjects);
        model.addAttribute("summary",      summary);
        model.addAttribute("hasWeak",      !weakSubjects.isEmpty());

        String weakNames = weakSubjects.stream()
                .map(SubjectRecommendationDto::subjectName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        model.addAttribute("weakNames", weakNames);

        return "recommendation/index";
    }

    // ──────────────────────────────────────────────
    //  REST API (Vanilla JS fetch 호출용)
    // ──────────────────────────────────────────────

    /**
     * GET /recommendation/api/all
     * 전체 과목 통계 + summary 반환 (JSON)
     */
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = resolveUserId(userDetails);
        return ResponseEntity.ok(Map.of(
                "subjects", recommendationService.getAllSubjectStats(userId),
                "summary",  recommendationService.getSummaryStats(userId)
        ));
    }

    /**
     * GET /recommendation/api/weak
     * 취약 과목만 반환 (사이드바 뱃지 숫자 실시간 갱신용)
     */
    @GetMapping("/api/weak")
    @ResponseBody
    public ResponseEntity<List<SubjectRecommendationDto>> getWeakSubjects(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = resolveUserId(userDetails);
        return ResponseEntity.ok(recommendationService.getWeakSubjects(userId));
    }
}
