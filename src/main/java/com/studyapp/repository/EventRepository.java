package com.studyapp.repository;

import com.studyapp.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByUserIdOrderByDueDateAsc(String userId);
    List<Event> findByUserIdAndDueDateGreaterThanEqualOrderByDueDateAsc(String userId, LocalDate date);

    List<Event> findByNotificationEnabledTrueAndCompletedFalse();
}
