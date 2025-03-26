package com.twentyone.steachserver.domain.studentQuiz.controller;

import com.twentyone.steachserver.domain.member.model.Student;
import com.twentyone.steachserver.domain.studentQuiz.dto.StudentQuizDto;
import com.twentyone.steachserver.domain.studentQuiz.dto.StudentQuizRequestDto;
import com.twentyone.steachserver.domain.studentQuiz.model.StudentQuiz;
import com.twentyone.steachserver.domain.studentQuiz.service.StudentQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "학생-퀴즈")
@RestController
@RequestMapping("/api/v1/studentsQuizzes")
@RequiredArgsConstructor
public class StudentQuizController {

    private final StudentQuizService studentQuizService;

    @Secured("ROLE_STUDENT")
    @Operation(summary = "[학생] 학생이 퀴즈를 풀면 퀴즈에 관한 정보 저장!", description = "무조건 200을 반환")
    @PostMapping("/{quizId}")
    public ResponseEntity<?> createStudentQuiz(@AuthenticationPrincipal Student student, @PathVariable("quizId") Integer quizId, @RequestBody StudentQuizRequestDto requestDto) throws Exception {
        StudentQuiz studentQuiz = studentQuizService.createStudentQuiz(student, quizId, requestDto);
        return ResponseEntity
                .status(HttpStatus.OK).body(StudentQuizDto.createStudentQuizDto(studentQuiz, student.getName()));
    }

}
