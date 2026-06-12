package com.studyapp.service;

import com.studyapp.model.Event;
import com.studyapp.model.ReviewSchedule;
import com.studyapp.repository.EventRepository;
import com.studyapp.repository.ReviewScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final Logger log = Logger.getLogger(NotificationScheduler.class.getName());
    private final EventRepository eventRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void checkDdayNotifications() {
        LocalDate today = LocalDate.now();
        List<Event> events = eventRepository.findAll();
        for (Event event : events) {
            if (event.isCompleted()) continue;
            if (event.getDueDate() == null) continue;
            long daysLeft = today.until(event.getDueDate()).getDays();
            List<Integer> notifyBefore = event.getNotifyBefore();
            if (notifyBefore != null && notifyBefore.contains((int) daysLeft)) {
                log.info("[Notification] D-" + daysLeft + " 알림: " + event.getTitle());
            }
        }
    }
}
