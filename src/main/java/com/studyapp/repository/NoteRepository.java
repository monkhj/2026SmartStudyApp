package com.studyapp.repository;

import com.studyapp.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findByUserId(String userId);
    List<Note> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Note> findAllByOrderByCreatedAtDesc();
}
