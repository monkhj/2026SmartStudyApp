package com.studyapp.service;

import com.studyapp.model.CourseInfo;
import com.studyapp.model.Schedule;
import com.studyapp.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ReviewScheduleService reviewScheduleService; // ★ 추가된 부분: 복습 일정 생성 서비스 주입

    public List<Schedule> getByUser(String userId) {
        return scheduleRepository.findByUserId(userId);
    }

    // ★ 수정된 부분: 단일 시간표 저장 시 복습 일정 자동 생성
    public Schedule save(Schedule s) {
        Schedule savedSchedule = scheduleRepository.save(s);
        reviewScheduleService.createReviewSchedules(savedSchedule); // 복습 일정 생성 트리거
        return savedSchedule;
    }

    public Schedule findById(String id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("시간표를 찾을 수 없습니다."));
    }

    public Schedule updateMemo(String id, String userId, String memo) {
        Schedule s = findById(id);
        if (!s.getUserId().equals(userId)) throw new SecurityException("권한이 없습니다.");
        s.setMemo(memo);
        return scheduleRepository.save(s);
    }

    public com.studyapp.model.Schedule getById(String id) {
        return scheduleRepository.findById(id).orElse(null);
    }

    public void delete(String id, String userId) {
        scheduleRepository.deleteByIdAndUserId(id, userId);
    }

    // ★ 수정된 부분: 일괄 등록 시에도 모든 과목에 대해 복습 일정 자동 생성
    public List<Schedule> bulkRegister(String userId, List<CourseInfo> courses) {
        List<Schedule> schedules = courses.stream()
                .map(c -> {
                    Schedule s = new Schedule();
                    s.setUserId(userId);
                    s.setSubjectName(c.getSubjectName());
                    s.setProfessor(c.getProfessor());
                    s.setRoom(c.getRoom());
                    s.setDayOfWeek(c.getDayOfWeek());
                    s.setStartHour(c.getStartHour());
                    s.setStartMinute(c.getStartMinute());
                    s.setEndHour(c.getEndHour());
                    s.setEndMinute(c.getEndMinute());
                    s.setColor(c.getColor());
                    return s;
                })
                .toList();
                
        List<Schedule> savedSchedules = scheduleRepository.saveAll(schedules);
        
        // 일괄 저장된 모든 시간표에 대해 각각 에빙하우스 복습 일정 생성
        for (Schedule saved : savedSchedules) {
            reviewScheduleService.createReviewSchedules(saved);
        }
        
        return savedSchedules;
    }
}