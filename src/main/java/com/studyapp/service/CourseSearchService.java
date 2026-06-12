package com.studyapp.service;

import com.studyapp.model.CourseInfo;
import java.util.List;

/**
 * 강의 검색 서비스 인터페이스
 */
public interface CourseSearchService {

    /**
     * 대학교명 + 학과명으로 수강 가능한 강의 목록 조회
     *
     * @param university 대학교명
     * @param department 학과명
     * @return 강의 목록
     */
    List<CourseInfo> searchCourses(String university, String department);
}