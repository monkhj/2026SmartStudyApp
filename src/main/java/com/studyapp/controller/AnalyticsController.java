package com.studyapp.controller;

import com.studyapp.dto.*;
import com.studyapp.dto.StatisticsResult;
import com.studyapp.dto.RankingResult;
import com.studyapp.model.Subject;
import com.studyapp.service.AnalyticsService;
import com.studyapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService      userService;

    // 현재 로그인 유저 ID 가져오기
    private String resolveUserId(UserDetails userDetails) {
        try {
            // 1. @AuthenticationPrincipal 시도
            if (userDetails != null) {
                return analyticsService.getUserIdByUsername(userDetails.getUsername());
            }
            // 2. SecurityContextHolder 시도
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !auth.getPrincipal().equals("anonymousUser")) {
                return analyticsService.getUserIdByUsername(auth.getName());
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger("AnalyticsController")
                .warning("userId 조회 실패: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/heatmap")
    public ResponseEntity<?> getHeatmap(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "365") int days
    ) {
        String userId = resolveUserId(user);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(analyticsService.getHeatmapData(userId, days));
    }

    @GetMapping("/activity-summary")
    public ResponseEntity<?> getActivitySummary(
            @AuthenticationPrincipal UserDetails user
    ) {
        String userId = resolveUserId(user);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of(
                "todayMinutes", analyticsService.getTodayMinutes(userId),
                "streakDays",   analyticsService.getStreakDays(userId)
        ));
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(
            @AuthenticationPrincipal UserDetails user
    ) {
        String userId = resolveUserId(user);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(analyticsService.getStatistics(userId));
    }

    @GetMapping("/wrong-answers")
    public ResponseEntity<?> getWrongAnswers(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String subjectId
    ) {
        String userId = resolveUserId(user);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(analyticsService.getWrongAnswers(userId, subjectId));
    }

    @GetMapping("/subjects")
    public ResponseEntity<?> getSubjects(
            @AuthenticationPrincipal UserDetails user
    ) {
        String userId = resolveUserId(user);
        if (userId == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(analyticsService.getSubjectsByUser(userId));
    }

    @GetMapping("/ranking")
    public ResponseEntity<?> getRanking(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "all") String scope
    ) {
        String userId = resolveUserId(user);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(analyticsService.getRankings(userId, scope, 20));
    }

    @GetMapping("/my-rank")
    public ResponseEntity<?> getMyRank(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "all") String scope
    ) {
        String userId = resolveUserId(user);
        if (userId == null) return ResponseEntity.status(401).build();
        RankingResult result = analyticsService.getMyRank(userId, scope);
        return result != null
                ? ResponseEntity.ok(result)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/wrong-answers/{id}")
    public ResponseEntity<?> deleteWrongAnswer(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user
    ) {
        String userId = resolveUserId(user);
        if (userId == null) return ResponseEntity.status(401).build();
        try {
            analyticsService.deleteWrongAnswer(userId, id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}