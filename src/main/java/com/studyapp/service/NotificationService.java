package com.studyapp.service;

import com.studyapp.model.Event;
import com.studyapp.model.ReviewSchedule;
import com.studyapp.repository.EventRepository;
import com.studyapp.repository.ReviewScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EventRepository eventRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;

    public List<Event> getUpcomingEvents(String userId) {
        return eventRepository.findByUserIdOrderByDueDateAsc(userId);
    }

    public List<ReviewSchedule> getPendingReviews(String userId) {
        return reviewScheduleRepository.findByUserId(userId);
    }
}
