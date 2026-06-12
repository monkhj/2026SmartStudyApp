package com.studyapp.service;

import com.studyapp.dto.*;
import com.studyapp.model.*;
import com.studyapp.repository.*;
import com.studyapp.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository       userRepository;
    private final StudyLogRepository   studyLogRepository;
    private final QuizResultRepository quizResultRepository;
    private final SubjectRepository    subjectRepository;
    private final ScheduleRepository   scheduleRepository;

    // ── 유저 ID 조회 ──────────────────────────────────
    public String getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + username));
    }

    // ── F-13: 히트맵 (날짜 → 공부 분) ──────────────────
    public Map<String, Integer> getHeatmapData(String userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days);
        LocalDate end   = today.plusDays(1); // Between은 end 미포함 → +1로 오늘 포함

        List<StudyLog> logs = studyLogRepository
                .findByUserIdAndDateBetween(userId, start, end);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return logs.stream().collect(Collectors.groupingBy(
                log -> log.getDate().format(fmt),
                Collectors.summingInt(StudyLog::getDurationMinutes)
        ));
    }

    // ── F-13: 오늘 공부 시간 ─────────────────────────
    public int getTodayMinutes(String userId) {
        return studyLogRepository
                .findByUserIdAndDate(userId, LocalDate.now())
                .stream()
                .filter(log -> "SESSION".equals(log.getActivityType()))
                .mapToInt(StudyLog::getDurationMinutes)
                .sum();
    }

    // ── F-13: 연속 학습일 (streak) ───────────────────
    public int getStreakDays(String userId) {
        LocalDate today = LocalDate.now();
        int streak = 0;

        for (int i = 0; i < 365; i++) {
            LocalDate date = today.minusDays(i);
            boolean studied = studyLogRepository
                    .findByUserIdAndDate(userId, date)
                    .stream()
                    .anyMatch(l -> l.getDurationMinutes() > 0);

            if (studied) {
                streak++;
            } else if (i > 0) {
                // 오늘(i=0)은 아직 0일 수 있으므로 어제부터 끊기면 종료
                break;
            }
        }
        return streak;
    }

    // ── F-14: 통계 전체 응답 (단일 API 호출용) ──────────
    public StatisticsResult getStatistics(String userId) {
        // Subject 컬렉션 대신 시간표(Schedule)에서 과목명 가져오기
        List<String> subjectNames = scheduleRepository.findByUserId(userId)
                .stream()
                .map(s -> s.getSubjectName())
                .filter(n -> n != null && !n.isBlank())
                .distinct()
                .collect(Collectors.toList());

        // 퀴즈 결과에 있는 과목도 추가 (시간표에 없는 경우)
        List<String> quizSubjects = quizResultRepository.findByUserIdOrderBySolvedAtAsc(userId)
                .stream()
                .map(QuizResult::getSubjectId)
                .filter(s -> s != null && !s.isBlank() && !s.equals("general"))
                .distinct()
                .collect(Collectors.toList());
        quizSubjects.forEach(s -> { if (!subjectNames.contains(s)) subjectNames.add(s); });

        List<Double>  radarData    = new ArrayList<>();
        List<Integer> weekData     = new ArrayList<>();
        List<Integer> monthData    = new ArrayList<>();

        LocalDate weekAgo  = LocalDate.now().minusDays(7);
        LocalDate monthAgo = LocalDate.now().minusDays(30);

        for (String subjectName : subjectNames) {
            // 정답률 계산 - subjectId = 과목명으로 저장됨
            List<QuizResult> results = quizResultRepository
                    .findByUserIdAndSubjectId(userId, subjectName);
            double accuracy = 0;
            if (!results.isEmpty()) {
                long total   = results.stream().mapToLong(QuizResult::getTotalCount).sum();
                long correct = results.stream().mapToLong(QuizResult::getCorrectCount).sum();
                accuracy = total > 0 ? Math.round((double) correct / total * 100.0) : 0;
            }
            radarData.add(accuracy);

            // 주간/월간 학습 시간 - subjectId = 과목명
            int wMin = studyLogRepository
                    .findByUserIdAndSubjectIdAndDateAfter(userId, subjectName, weekAgo)
                    .stream().mapToInt(StudyLog::getDurationMinutes).sum();
            weekData.add(wMin);

            int mMin = studyLogRepository
                    .findByUserIdAndSubjectIdAndDateAfter(userId, subjectName, monthAgo)
                    .stream().mapToInt(StudyLog::getDurationMinutes).sum();
            monthData.add(mMin);
        }

        // 점수 추이: 최근 8개 퀴즈 세션 평균 점수
        List<QuizResult> allResults = quizResultRepository
                .findByUserIdOrderBySolvedAtAsc(userId);

        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("M/d");
        // 날짜별로 그룹화해서 평균 점수 계산
        Map<String, List<QuizResult>> byDate = allResults.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getSolvedAt().format(labelFmt),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<String>  trendLabels = new ArrayList<>();
        List<Integer> trendAll    = new ArrayList<>();

        byDate.entrySet().stream()
                .skip(Math.max(0, byDate.size() - 8)) // 최근 8개
                .forEach(entry -> {
                    trendLabels.add(entry.getKey());
                    double avg = entry.getValue().stream()
                            .mapToDouble(r -> r.getTotalCount() > 0
                                    ? (double) r.getCorrectCount() / r.getTotalCount() * 100
                                    : 0)
                            .average().orElse(0);
                    trendAll.add((int) Math.round(avg));
                });

        return new StatisticsResult(
                subjectNames, radarData,
                subjectNames, weekData, monthData,
                trendLabels, trendAll
        );
    }

    // ── F-10: 오답 목록 ──────────────────────────────
    public List<WrongAnswerItem> getWrongAnswers(String userId, String subjectId) {
        List<QuizResult> results = (subjectId != null && !subjectId.isBlank())
                ? quizResultRepository.findWrongAnswersByUserIdAndSubjectId(userId, subjectId)
                : quizResultRepository.findWrongAnswersByUserId(userId);

        // 과목 이름 캐시 (N+1 방지)
        Map<String, String> subjectNameMap = subjectRepository.findByUserId(userId)
                .stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));

        return results.stream()
                .map(qr -> new WrongAnswerItem(
                        qr.getId(),
                        qr.getSubjectId(),
                        subjectNameMap.getOrDefault(qr.getSubjectId(), qr.getSubjectId() != null ? qr.getSubjectId() : "기타"),
                        qr.getQuestionText(),
                        qr.getUserAnswer(),
                        qr.getCorrectAnswer(),
                        qr.getExplanation(),
                        qr.getSolvedAt()
                ))
                .toList(); // Java 16+ stream.toList()
    }

    // ── F-15: 소셜 랭킹 ──────────────────────────────
    public List<RankingEntry> getRankings(String userId, String scope, int limit) {
        String myMajor = userRepository.findById(userId)
                .map(User::getMajor).orElse("");

        List<User> users = "major".equals(scope)
                ? userRepository.findByMajor(myMajor)
                : userRepository.findAll();

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);

        List<RankingEntry> entries = users.stream().map(user -> {
            int totalMin = studyLogRepository
                    .findByUserIdAndDateAfter(user.getId(), monthStart)
                    .stream()
                    .mapToInt(StudyLog::getDurationMinutes)
                    .sum();

            return RankingEntry.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .major(user.getMajor() != null ? user.getMajor() : "")
                    .totalMinutes(totalMin)
                    .isMe(user.getId().equals(userId))
                    .build();
        }).collect(Collectors.toList());

        // 내림차순 정렬
        entries.sort(Comparator.comparingInt(RankingEntry::getTotalMinutes).reversed());

        // 상위 N개 자르고 percentage 계산
        List<RankingEntry> top = entries.stream().limit(limit).collect(Collectors.toList());
        int maxMin = top.isEmpty() ? 1 : Math.max(top.get(0).getTotalMinutes(), 1);

        top.forEach(e -> e.setPercentage(
                (int) Math.round((double) e.getTotalMinutes() / maxMin * 100)
        ));

        return top;
    }

    // ── F-15: 내 순위 ────────────────────────────────
    public RankingResult getMyRank(String userId, String scope) {
        List<RankingEntry> all = getRankings(userId, scope, Integer.MAX_VALUE);
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).isMe()) {
                RankingEntry me = all.get(i);
                return new RankingResult(i + 1, me.getMajor(), me.getTotalMinutes());
            }
        }
        return null;
    }

    // ── 과목 목록 (필터 드롭다운용) ──────────────────
    public void deleteWrongAnswer(String userId, String id) {
        QuizResult qr = quizResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("오답을 찾을 수 없습니다."));
        if (!userId.equals(qr.getUserId()))
            throw new IllegalArgumentException("권한이 없습니다.");
        quizResultRepository.deleteById(id);
    }

    public List<Subject> getSubjectsByUser(String userId) {
        // 시간표 과목명을 Subject 형태로 반환
        List<Subject> result = new java.util.ArrayList<>();
        scheduleRepository.findByUserId(userId).stream()
                .map(s -> s.getSubjectName())
                .filter(n -> n != null && !n.isBlank())
                .distinct()
                .forEach(name -> {
                    Subject s = new Subject();
                    s.setId(name);   // id = 과목명으로 통일
                    s.setName(name);
                    s.setUserId(userId);
                    result.add(s);
                });
        return result;
    }
}
