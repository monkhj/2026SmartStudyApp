package com.studyapp.recommendation;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface RecommendationQuizResultRepository extends MongoRepository<RecommendationQuizResult, String> {
    List<RecommendationQuizResult> findByUserId(String userId);
}
