package com.twentyone.steachserver.domain.quiz.controller;

import com.twentyone.steachserver.domain.auth.model.LoginCredential;
import com.twentyone.steachserver.domain.member.model.Teacher;
import com.twentyone.steachserver.domain.quiz.dto.*;
import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.service.QuizService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "퀴즈")
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @Secured("ROLE_TEACHER")
    @Operation(summary = "[강사] 퀴즈 하나 생성")
    @PostMapping("/{lectureId}/one") //TODO url 수정
    public ResponseEntity<QuizResponseDto> createQuiz(@AuthenticationPrincipal Teacher teacher,
                                                      @PathVariable("lectureId") Integer lectureId,
                                                      @RequestBody @Valid QuizRequestDto request) {
        QuizResponseDto quizResponseDto = quizService.createOneQuiz(teacher, lectureId, request);

        return ResponseEntity.ok(quizResponseDto);
    }

    @Secured("ROLE_TEACHER")
    @Operation(summary = "[강사] 퀴즈 여러 개 생성!", description = "성공시 200 반환, 실패시 500 INTERNAL_SERVER_ERROR 반환")
    @PostMapping("/{lectureId}")
    public ResponseEntity<QuizListResponseDto> createQuizList(@AuthenticationPrincipal Teacher teacher, @PathVariable("lectureId")Integer lectureId, @RequestBody @Valid QuizListRequestDto request) {
        //TODO 수정이랑 너무 똑같다
        QuizListResponseDto dto = quizService.modifyManyQuiz(teacher, lectureId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "[강사?] 퀴즈 조회!", description = "성공시 200 반환, 실패시 204 NOT_FOUND 반환")
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponseDto> getQuizResponseDto(@PathVariable("quizId") Integer quizId) {
        return quizService.findById(quizId)
                .map(quiz -> ResponseEntity.ok().body(quizService.mapToDto(quiz)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Secured("ROLE_TEACHER")
    @Operation(summary = "[강사] 퀴즈 여러 개 수정", description = "성공시 200 반환<br/>null로 들어올 경우 변경되지 않습니다. choices에 변경사항이 있는 경우 answers는 null이 되면 안됩니다.")
    @PutMapping("/{lectureId}")
    public ResponseEntity<QuizListResponseDto> modifyManyQuiz(@AuthenticationPrincipal Teacher teacher, @PathVariable("lectureId") Integer lectureId, @RequestBody @Valid QuizListRequestDto dto) {
        QuizListResponseDto quizListResponseDto = quizService.modifyManyQuiz(teacher, lectureId, dto);

        return ResponseEntity.ok(quizListResponseDto);
    }

    @Secured("ROLE_TEACHER")
    @Operation(summary = "[강사] 퀴즈 수정", description = "성공시 200 반환<br/>null로 들어올 경우 변경되지 않습니다. choices에 변경사항이 있는 경우 answers는 null이 되면 안됩니다.")
    @PatchMapping("/{quizId}/one")
    public ResponseEntity<QuizResponseDto> modifyQuiz(@AuthenticationPrincipal Teacher teacher, @PathVariable("quizId") Integer quizId, @RequestBody QuizRequestDto dto) {
        QuizResponseDto quizResponseDto = quizService.modifyQuiz(teacher, quizId, dto);

        return ResponseEntity.ok(quizResponseDto);
    }

    @Operation(summary = "[강사?] 강의에 대한 퀴즈 조회!", description = "무조건 200 반환")
    @GetMapping("/lecture/{lectureId}")
    public ResponseEntity<QuizzesResponseDto> getQuizzesResponseDto(@PathVariable("lectureId")Integer lectureId) {
        List<Quiz> quizzes = quizService.findAllByLectureId(lectureId);

        // Use Stream API to map entities to DTOs
        List<QuizResponseDto> quizResponseDtos = quizzes.stream()
                .map(quizService::mapToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(QuizzesResponseDto.of(quizResponseDtos));
    }

    @Secured("ROLE_TEACHER")
    @DeleteMapping("/{quiz_id}")
    public ResponseEntity<QuizResponseDto> deleteQuiz(@PathVariable("quiz_id")Integer quizId, @AuthenticationPrincipal Teacher teacher) {
        quizService.delete(quizId, teacher);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{quiz_id}/statistic")
    public ResponseEntity<QuizStatisticDto> getStatistics(@PathVariable("quiz_id")Integer quizId, @AuthenticationPrincipal LoginCredential loginCredential) {
        QuizStatisticDto dto = quizService.getStatistics(quizId);

        return ResponseEntity.ok(dto);
    }
}
