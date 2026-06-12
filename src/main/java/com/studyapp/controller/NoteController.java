package com.studyapp.controller;

import com.studyapp.model.Note;
import com.studyapp.model.QuizResult;
import com.studyapp.dto.NoteRequest;
import com.studyapp.dto.QuizResultRequest;
import com.studyapp.repository.NoteRepository;
import com.studyapp.repository.QuizResultRepository;
import com.studyapp.repository.ScheduleRepository;
import com.studyapp.repository.SubjectRepository;
import com.studyapp.service.AIBridgeService;
import com.studyapp.service.ReviewScheduler;
import com.studyapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private static final Logger log = Logger.getLogger("NoteController");

    private final AIBridgeService      aiBridgeService;
    private final NoteRepository       noteRepository;
    private final QuizResultRepository quizResultRepository;
    private final SubjectRepository    subjectRepository;
    private final ScheduleRepository   scheduleRepository;
    private final ReviewScheduler      reviewScheduler;
    private final UserService          userService;

    private String resolveUserId(UserDetails userDetails) {
        try {
            if (userDetails != null)
                return userService.findByUsername(userDetails.getUsername()).getId();
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !auth.getPrincipal().equals("anonymousUser"))
                return userService.findByUsername(auth.getName()).getId();
        } catch (Exception ignored) {}
        return null;
    }

    @GetMapping
    public String listPage(Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        if (userId == null) return "redirect:/auth/login";
        List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        model.addAttribute("notes", notes);
        return "notes/index";
    }

    @GetMapping("/new")
    public String newNotePage(Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("note", null);
        addSubjectsToModel(model, userDetails);
        return "notes/detail";
    }

    @GetMapping("/{id}")
    public String detailPage(@PathVariable String id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) return "redirect:/notes";
        model.addAttribute("note", note);
        addSubjectsToModel(model, userDetails);
        return "notes/detail";
    }

    // 시간표 과목 목록 + 기타를 model에 추가
    private void addSubjectsToModel(Model model, UserDetails userDetails) {
        try {
            String userId = resolveUserId(userDetails);
            java.util.List<String> subjects = new java.util.ArrayList<>();
            if (userId != null) {
                scheduleRepository.findByUserId(userId).stream()
                        .map(s -> s.getSubjectName())
                        .filter(n -> n != null && !n.isBlank())
                        .distinct()
                        .forEach(subjects::add);
            }
            subjects.add("기타");
            model.addAttribute("subjects", subjects);
        } catch (Exception e) {
            model.addAttribute("subjects", java.util.List.of("기타"));
        }
    }

    @PostMapping("/analyze")
    @ResponseBody
    public ResponseEntity<Note> analyzeNote(@RequestBody NoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = request.getUserId();
        if (userId == null || userId.isBlank() || userId.equals("user01")) {
            String r = resolveUserId(userDetails);
            if (r != null) userId = r;
        }
        Note note = aiBridgeService.analyze(userId, request.getSubjectId(),
                request.getTitle(), request.getText());
        Note saved = noteRepository.save(note);
        if (saved.getUserId() != null) reviewScheduler.schedule(saved.getUserId(), saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/analyze")
    @ResponseBody
    public ResponseEntity<Note> analyzeExistingNote(@PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) return ResponseEntity.notFound().build();
        if (note.getUserId() == null || note.getUserId().isBlank() || note.getUserId().equals("user01")) {
            String r = resolveUserId(userDetails);
            if (r != null) note.setUserId(r);
        }
        Note analyzed = aiBridgeService.analyze(note.getUserId(), note.getSubjectId(),
                note.getTitle(), note.getOriginalText());
        note.setSummary(analyzed.getSummary());
        note.setKeywords(analyzed.getKeywords());
        note.setQuestions(analyzed.getQuestions());
        Note saved = noteRepository.save(note);
        if (saved.getUserId() != null) reviewScheduler.schedule(saved.getUserId(), saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<Note> saveNote(@RequestBody NoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Note note = new Note();
        note.setTitle(request.getTitle());
        String userId = request.getUserId();
        if (userId == null || userId.isBlank() || userId.equals("user01")) {
            String r = resolveUserId(userDetails);
            if (r != null) userId = r;
        }
        note.setUserId(userId);
        note.setSubjectId(request.getSubjectId() != null ? request.getSubjectId() : "general");
        note.setOriginalText(request.getText());
        note.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(noteRepository.save(note));
    }

    @PostMapping("/{id}/quiz-result")
    @ResponseBody
    public ResponseEntity<Void> saveQuizResult(@PathVariable String id,
            @RequestBody QuizResultRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) return ResponseEntity.notFound().build();

        note.setWrongAnswerIndices(request.getWrongIndices());
        if (note.getUserId() == null || note.getUserId().isBlank() || note.getUserId().equals("user01")) {
            String r = resolveUserId(userDetails);
            if (r != null) note.setUserId(r);
        }
        noteRepository.save(note);

        List<QuizResultRequest.QuizAnswerItem> answers = request.getAnswers();
        log.info("[QuizResult] answers=" + (answers != null ? answers.size() : 0) + " noteId=" + id);

        if (answers != null && !answers.isEmpty()) {
            final String finalUserId = note.getUserId();
            // subjectId: 노트의 subjectId 그대로 사용 (과목명이나 ID 둘 다 허용)
            final String subjectId = note.getSubjectId() != null ? note.getSubjectId() : "general";
            long totalCount   = answers.size();
            long correctCount = answers.stream().filter(QuizResultRequest.QuizAnswerItem::isCorrect).count();

            log.info("[QuizResult] userId=" + finalUserId + " subjectId=" + subjectId
                    + " total=" + totalCount + " correct=" + correctCount);

            for (QuizResultRequest.QuizAnswerItem ans : answers) {
                // 정답/오답 모두 저장 (분석통계 정답률 계산에 필요)
                String expl = (ans.getExplanation() != null && !ans.getExplanation().isBlank())
                        ? ans.getExplanation()
                        : "정답은 [" + ans.getCorrectAnswer() + "]입니다.";

                QuizResult qr = QuizResult.builder()
                        .userId(finalUserId)
                        .subjectId(subjectId)
                        .questionText(ans.getQuestionText())
                        .userAnswer(ans.getUserAnswer())
                        .correctAnswer(ans.getCorrectAnswer())
                        .explanation(expl)
                        .correct(ans.isCorrect())
                        .totalCount((int) totalCount)
                        .correctCount((int) correctCount)
                        .solvedAt(LocalDateTime.now())
                        .build();
                quizResultRepository.save(qr);
            }
            log.info("[QuizResult] 저장 완료 " + answers.size() + "개");
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        noteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
