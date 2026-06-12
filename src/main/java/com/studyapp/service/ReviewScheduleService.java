package com.studyapp.service;
 
import com.studyapp.model.ReviewSchedule;
import com.studyapp.model.Schedule;
import com.studyapp.repository.ReviewScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
 
@Service
@RequiredArgsConstructor
public class ReviewScheduleService {
    private static final Logger log = Logger.getLogger(ReviewScheduleService.class.getName());
    private final ReviewScheduleRepository reviewScheduleRepository;
 
    public ReviewSchedule createReviewSchedules(Schedule schedule) {
        ReviewSchedule rs = new ReviewSchedule();
        rs.setUserId(schedule.getUserId());
        rs.setScheduleId(schedule.getId());
        rs.setSubjectName(schedule.getSubjectName());
        LocalDateTime now = LocalDateTime.now();
        rs.setReview1Day(now.plusDays(1));
        rs.setReview7Days(now.plusDays(7));
        rs.setReview30Days(now.plusDays(30));
        rs.setStatus("pending");
        rs.setCreatedAt(now);
        return reviewScheduleRepository.save(rs);
    }
 
    public List<ReviewSchedule> findByUserId(String userId) {
        return reviewScheduleRepository.findByUserId(userId);
    }
}