package com.studyapp.service;

import com.studyapp.model.ReviewSchedule;
import com.studyapp.repository.ReviewScheduleRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ReviewScheduler {

    private final ReviewScheduleRepository reviewScheduleRepository;

    public ReviewScheduler(ReviewScheduleRepository reviewScheduleRepository) {
        this.reviewScheduleRepository = reviewScheduleRepository;
    }

    public ReviewSchedule schedule(String userId, String noteId) {
        LocalDateTime now = LocalDateTime.now();
        ReviewSchedule schedule = new ReviewSchedule();
        schedule.setUserId(userId);
        schedule.setNoteId(noteId);
        schedule.setReview1Day(now.plusDays(1));
        schedule.setReview7Days(now.plusDays(7));
        schedule.setReview30Days(now.plusDays(30));
        schedule.setStatus("pending");
        schedule.setCreatedAt(now);
        return reviewScheduleRepository.save(schedule);
    }
}
