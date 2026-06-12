package com.studyapp.repository;

import com.studyapp.model.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    List<Schedule> findByUserId(String userId);
    void deleteByIdAndUserId(String id, String userId);
}
