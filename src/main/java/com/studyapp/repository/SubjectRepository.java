package com.studyapp.repository;

import com.studyapp.model.Subject;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubjectRepository extends MongoRepository<Subject, String> {

    List<Subject> findByUserId(String userId);
}
