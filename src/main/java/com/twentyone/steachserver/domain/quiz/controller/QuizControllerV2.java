package com.twentyone.steachserver.domain.quiz.controller;

import com.twentyone.steachserver.domain.quiz.dto.QuizResponseDtoV2;
import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "퀴즈")
@RestController
@RequestMapping("/api/v2/quizzes")
@RequiredArgsConstructor
public class QuizControllerV2 {
    private final QuizService quizService;

    @Operation(summary = "퀴즈 조회", description = "성공시 200 반환, 실패시 204 NOT_FOUND 반환")
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponseDtoV2> getQuizResponseDto(@PathVariable("quizId") Integer quizId) {
        Quiz quiz = quizService.findById(quizId)
                .orElseGet(() -> null);

        if (quiz == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok().body(QuizResponseDtoV2.fromDomain(quiz));
    }
}
