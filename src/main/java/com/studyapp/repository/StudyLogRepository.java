package com.studyapp.repository;

import com.studyapp.model.StudyLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudyLogRepository extends MongoRepository<StudyLog, String> {

    // 히트맵용: 날짜 범위 전체 로그
    List<StudyLog> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);

    // 오늘 로그
    List<StudyLog> findByUserIdAndDate(String userId, LocalDate date);

    // 과목별 + 기간 필터 (막대 차트용)
    List<StudyLog> findByUserIdAndSubjectIdAndDateAfter(
        String userId, String subjectId, LocalDate after
    );

    // 랭킹용: 특정 날짜 이후 전체 로그
    List<StudyLog> findByUserIdAndDateAfter(String userId, LocalDate after);
}
