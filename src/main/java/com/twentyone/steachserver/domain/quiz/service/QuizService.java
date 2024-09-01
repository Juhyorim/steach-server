package com.twentyone.steachserver.domain.quiz.service;

import com.twentyone.steachserver.domain.member.model.Teacher;
import com.twentyone.steachserver.domain.quiz.dto.*;
import com.twentyone.steachserver.domain.quiz.model.Quiz;

import java.util.List;
import java.util.Optional;

public interface QuizService {
    // Quiz methods
    QuizListResponseDto createQuizList(Integer lectureId, QuizListRequestDto request) throws RuntimeException;

    Optional<Quiz> findById(Integer quizId);

    QuizResponseDto mapToDto(Quiz quiz);

    List<Quiz> findAllByLectureId(Integer lectureId);

    void delete(Integer quizId, Teacher teacher);

    QuizResponseDto modifyQuiz(Teacher teacher, Integer quizId, QuizRequestDto dto);

    QuizListResponseDto modifyManyQuiz(Teacher teacher, Integer quizId, QuizListRequestDto dto);

    QuizStatisticDto getStatistics(Integer quizId);

    QuizResponseDto createOneQuiz(Teacher teacher, Integer lectureId, QuizRequestDto request);
}

