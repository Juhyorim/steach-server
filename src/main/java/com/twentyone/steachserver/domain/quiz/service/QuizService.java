package com.twentyone.steachserver.domain.quiz.service;

import com.twentyone.steachserver.domain.lecture.model.Lecture;
import com.twentyone.steachserver.domain.member.model.Teacher;
import com.twentyone.steachserver.domain.quiz.dto.*;
import com.twentyone.steachserver.domain.quiz.model.Quiz;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;


public interface QuizService {
    @Transactional
    void startQuiz(Integer quizId, Teacher teacher);

    // Quiz methods
    QuizListResponseDto createQuizList(Integer lectureId, QuizListRequestDto request) throws RuntimeException;

    @Transactional
    Quiz createQuiz(Lecture lecture, QuizRequestDto quizRequestDto);

    @Transactional
    Quiz createQuizV2(Teacher teacher, Integer lectureId, QuizRequestDtoV2 quizRequestDto);

    Optional<Quiz> findById(Integer quizId);

    QuizResponseDto mapToDto(Quiz quiz);

    List<Quiz> findAllByLectureId(Integer lectureId);

    void delete(Integer quizId, Teacher teacher);

    QuizResponseDto modifyQuiz(Teacher teacher, Integer quizId, QuizRequestDto dto);

    QuizListResponseDto modifyManyQuiz(Teacher teacher, Integer quizId, QuizListRequestDto dto);

    QuizStatisticDto getStatistics(Integer quizId);

    @Transactional
    QuizStatisticDto getStatisticsV2(Integer quizId);
}

