package com.studyapp.controller;

import com.studyapp.model.CourseInfo;
import com.studyapp.model.Note;
import com.studyapp.model.Schedule;
import com.studyapp.model.User;
import com.studyapp.repository.NoteRepository;
import com.studyapp.repository.QuizResultRepository;
import com.studyapp.repository.StudyLogRepository;
import com.studyapp.service.ScheduleService;
import com.studyapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private static final Logger log = Logger.getLogger("ScheduleController");

    private final ScheduleService      scheduleService;
    private final NoteRepository       noteRepository;
    private final QuizResultRepository quizResultRepository;
    private final StudyLogRepository   studyLogRepository;
    private final UserService          userService;

    @GetMapping
    public String page(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userService.findByUsername(ud.getUsername());
        model.addAttribute("schedules", scheduleService.getByUser(user.getId()));
        model.addAttribute("user", user);
        return "schedule/index";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> add(@AuthenticationPrincipal UserDetails ud,
                                  @RequestBody Map<String, Object> body) {
        User user = userService.findByUsername(ud.getUsername());
        Schedule s = new Schedule();
        s.setUserId(user.getId());
        s.setSubjectName((String) body.get("subjectName"));
        s.setColor((String) body.get("color"));
        s.setDayOfWeek((Integer) body.get("dayOfWeek"));
        s.setStartHour((Integer) body.get("startHour"));
        s.setStartMinute((Integer) body.get("startMinute"));
        s.setEndHour((Integer) body.get("endHour"));
        s.setEndMinute((Integer) body.get("endMinute"));
        s.setProfessor((String) body.getOrDefault("professor", ""));
        s.setRoom((String) body.getOrDefault("room", ""));
        return ResponseEntity.ok(scheduleService.save(s));
    }

    @PostMapping("/bulk-add")
    @ResponseBody
    public ResponseEntity<?> bulkAdd(@AuthenticationPrincipal UserDetails ud,
                                      @RequestBody List<CourseInfo> courses) {
        User user = userService.findByUsername(ud.getUsername());
        List<Schedule> saved = scheduleService.bulkRegister(user.getId(), courses);
        return ResponseEntity.ok(Map.of(
                "count", saved.size(),
                "schedules", saved,
                "message", saved.size() + "개 강의가 시간표에 추가되었습니다."
        ));
    }

    @PostMapping("/{id}/memo")
    @ResponseBody
    public ResponseEntity<?> memo(@AuthenticationPrincipal UserDetails ud,
                                   @PathVariable String id,
                                   @RequestBody Map<String, String> body) {
        User user = userService.findByUsername(ud.getUsername());
        return ResponseEntity.ok(scheduleService.updateMemo(id, user.getId(), body.get("memo")));
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> get(@PathVariable String id) {
        return ResponseEntity.ok(scheduleService.findById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails ud,
                                     @PathVariable String id) {
        User user = userService.findByUsername(ud.getUsername());
        String userId = user.getId();

        try {
            // 삭제 전 과목명 가져오기
            Schedule schedule = scheduleService.findById(id);
            if (schedule != null && schedule.getSubjectName() != null) {
                String subjectName = schedule.getSubjectName();
                log.info("[Schedule] 과목 삭제: " + subjectName + " userId=" + userId);

                // 1. 관련 노트 삭제
                List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
                notes.stream()
                    .filter(n -> subjectName.equals(n.getSubjectId()))
                    .forEach(n -> {
                        noteRepository.delete(n);
                        log.info("[Schedule] 노트 삭제: " + n.getTitle());
                    });

                // 2. 관련 오답 삭제
                quizResultRepository.findByUserIdAndSubjectId(userId, subjectName)
                    .forEach(q -> quizResultRepository.delete(q));

                // 3. 관련 공부시간 삭제
                studyLogRepository
                    .findByUserIdAndSubjectIdAndDateAfter(userId, subjectName,
                        LocalDate.now().minusYears(5))
                    .forEach(s -> studyLogRepository.delete(s));

                log.info("[Schedule] 관련 데이터 삭제 완료: " + subjectName);
            }
        } catch (Exception e) {
            log.warning("[Schedule] 관련 데이터 삭제 실패: " + e.getMessage());
        }

        // 시간표에서 해당 강의 삭제
        scheduleService.delete(id, userId);
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }
}