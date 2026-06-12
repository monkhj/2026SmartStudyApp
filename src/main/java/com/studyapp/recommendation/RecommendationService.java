package com.studyapp.recommendation;

import com.studyapp.model.QuizResult;
import com.studyapp.repository.QuizResultRepository;
import com.studyapp.repository.ScheduleRepository;
import com.studyapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final double WEAK_THRESHOLD = 60.0;

    private final QuizResultRepository quizResultRepository;
    private final UserRepository       userRepository;
    private final ScheduleRepository   scheduleRepository;

    // userId 그대로 사용 (Controller에서 이미 변환)
    public List<SubjectRecommendationDto> getAllSubjectStats(String userId) {
        // 현재 시간표에 있는 과목명만 허용
        Set<String> validSubjects = scheduleRepository.findByUserId(userId)
                .stream()
                .map(s -> s.getSubjectName())
                .filter(n -> n != null && !n.isBlank())
                .collect(Collectors.toSet());

        List<QuizResult> results = quizResultRepository.findByUserIdOrderBySolvedAtAsc(userId);
        if (results.isEmpty()) return List.of();

        // 시간표에 있는 과목만 필터링
        Map<String, List<QuizResult>> bySubject = results.stream()
                .filter(qr -> qr.getSubjectId() != null
                        && !qr.getSubjectId().isBlank()
                        && (validSubjects.isEmpty() || validSubjects.contains(qr.getSubjectId())))
                .collect(Collectors.groupingBy(QuizResult::getSubjectId));

        return bySubject.entrySet().stream()
                .map(e -> {
                    List<QuizResult> sr = e.getValue();
                    long totalQ   = sr.stream().mapToLong(QuizResult::getTotalCount).sum();
                    long correctQ = sr.stream().mapToLong(QuizResult::getCorrectCount).sum();
                    double accuracy = totalQ > 0
                            ? Math.round((double) correctQ / totalQ * 1000.0) / 10.0
                            : 0.0;
                    return new SubjectRecommendationDto(
                            e.getKey(),
                            accuracy,
                            sr.size(),
                            accuracy < WEAK_THRESHOLD,
                            SubjectRecommendationDto.resolveLevel(accuracy)
                    );
                })
                .sorted(Comparator.comparingDouble(SubjectRecommendationDto::averageAccuracy))
                .collect(Collectors.toList());
    }

    public List<SubjectRecommendationDto> getWeakSubjects(String userId) {
        return getAllSubjectStats(userId).stream()
                .filter(SubjectRecommendationDto::isWeak)
                .collect(Collectors.toList());
    }

    public record SummaryStats(
            int totalSubjects,
            int weakSubjectCount,
            double overallAvgAccuracy,
            long totalQuizCount
    ) {}

    public SummaryStats getSummaryStats(String userId) {
        List<SubjectRecommendationDto> all = getAllSubjectStats(userId);
        int    totalSubjects  = all.size();
        int    weakCount      = (int) all.stream().filter(SubjectRecommendationDto::isWeak).count();
        long   totalQuizCount = all.stream().mapToLong(SubjectRecommendationDto::quizCount).sum();
        double overallAvg     = all.isEmpty() ? 0.0
                : Math.round(all.stream()
                        .mapToDouble(SubjectRecommendationDto::averageAccuracy)
                        .average().orElse(0.0) * 10.0) / 10.0;
        return new SummaryStats(totalSubjects, weakCount, overallAvg, totalQuizCount);
    }
}
