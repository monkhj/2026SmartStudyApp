package com.studyapp.repository;

import com.studyapp.model.QuizResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface QuizResultRepository extends MongoRepository<QuizResult, String> {

    // 과목별 전체 결과 (정답률 집계용)
    List<QuizResult> findByUserIdAndSubjectId(String userId, String subjectId);

    // F-10: 오답만 조회 (correct = false)
    @Query("{ 'userId': ?0, 'correct': false }")
    List<QuizResult> findWrongAnswersByUserId(String userId);

    // F-10: 과목 필터 + 오답
    @Query("{ 'userId': ?0, 'subjectId': ?1, 'correct': false }")
    List<QuizResult> findWrongAnswersByUserIdAndSubjectId(String userId, String subjectId);

    // 점수 추이용: 사용자 전체 퀴즈 결과 (최신순)
    List<QuizResult> findByUserIdOrderBySolvedAtAsc(String userId);

    // 특정 과목 점수 추이
    List<QuizResult> findByUserIdAndSubjectIdOrderBySolvedAtAsc(String userId, String subjectId);
}
