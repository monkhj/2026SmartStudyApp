package com.studyapp.repository;

import com.studyapp.model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
    // 특정 타겟(일정)에 특정 타입의 알림이 이미 발송되었는지 확인 (중복 발송 방지용)
    boolean existsByTargetIdAndType(String targetId, String type);
}