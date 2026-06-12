package com.studyapp.controller;

import com.studyapp.model.Event;
import com.studyapp.model.User;
import com.studyapp.service.EventService;
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

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final UserService userService;
@GetMapping
public String page(@AuthenticationPrincipal UserDetails ud, Model model) {
    User user = userService.findByUsername(ud.getUsername());
    
    // 기존에 있던 것
    model.addAttribute("eventsWithDday", eventService.getWithDday(user.getId()));
    
    // ★ [수정] 전체 일정도 D-Day 계산 결과와 매핑하여 보냅니다.
    List<Map<String, Object>> allEventsWithDday = eventService.getAll(user.getId()).stream()
            .map(e -> Map.of("event", e, "dday", eventService.calcDday(e.getDueDate())))
            .toList();
            
    model.addAttribute("allEvents", allEventsWithDday); 
    return "schedule/events";
}

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> add(@AuthenticationPrincipal UserDetails ud, @RequestBody Map<String, Object> body) {
        User user = userService.findByUsername(ud.getUsername());
        Event e = new Event();
        e.setUserId(user.getId());
        e.setTitle((String) body.get("title"));
        e.setType((String) body.get("type"));
        e.setSubjectName((String) body.getOrDefault("subjectName", ""));
        e.setDueDate(LocalDate.parse((String) body.get("dueDate")));
        e.setDescription((String) body.getOrDefault("description", ""));


        if (body.containsKey("notificationEnabled")) {
            e.setNotificationEnabled((Boolean) body.get("notificationEnabled"));
        }
        if (body.containsKey("notifyBefore") && body.get("notifyBefore") != null) {
          
            @SuppressWarnings("unchecked")
            List<Integer> notifyBefore = (List<Integer>) body.get("notifyBefore");
            e.setNotifyBefore(notifyBefore);
        }

       eventService.save(e); // DB에 저장은 그대로 진행
        return ResponseEntity.ok(Map.of("message", "일정이 성공적으로 추가되었습니다.")); // 변환하기 쉬운 단순 메시지만 반환
    }

    @PostMapping("/{id}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggle(@AuthenticationPrincipal UserDetails ud, @PathVariable String id) {
        User user = userService.findByUsername(ud.getUsername());
        eventService.toggleComplete(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "변경되었습니다."));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable String id) {
        eventService.delete(id);
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }
}