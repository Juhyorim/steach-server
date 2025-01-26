package com.twentyone.steachserver.domain.studentQuiz.service;

import com.twentyone.steachserver.domain.lecture.validator.LectureValidator;
import com.twentyone.steachserver.domain.member.model.Student;
import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.model.QuizStatistics;
import com.twentyone.steachserver.domain.quiz.repository.QuizRepository;
import com.twentyone.steachserver.domain.quiz.repository.QuizStatisticsRepository;
import com.twentyone.steachserver.domain.studentQuiz.dto.StudentQuizRequestDto;
import com.twentyone.steachserver.domain.studentQuiz.model.StudentQuiz;
import com.twentyone.steachserver.domain.studentQuiz.model.StudentQuizId;
import com.twentyone.steachserver.domain.studentQuiz.repository.StudentQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudentQuizServiceImpl implements StudentQuizService {
    private final StudentQuizRepository studentQuizzesRepository;
    private final QuizRepository quizRepository;
    private final QuizStatisticsRepository quizStatisticsRepository;

    @Secured("ROLE_STUDENT")
    @Transactional
    public StudentQuiz createStudentQuiz(Student student, Integer quizId, StudentQuizRequestDto requestDto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈입니다."));

        //lectureValidator.validateFinishLecture(quiz.getLecture());
        //lectureValidator.validateQuizOfLectureAuth(quiz, student);

        Optional<StudentQuiz> studentQuiz = studentQuizzesRepository.findById(
                StudentQuizId.createStudentQuizId(student.getId(), quizId));
        if (studentQuiz.isPresent()) {
            throw new IllegalArgumentException("이미 점수가 존재합니다.");
        }

        StudentQuiz newStudentQuiz = StudentQuiz.createStudentQuiz(student, quiz, requestDto);
        studentQuizzesRepository.save(newStudentQuiz);

        //통계생성 - 실패해도 계속 진행하도록 처리
        try {
            createStatistics(student, requestDto, quiz, newStudentQuiz);
        } catch (RuntimeException e) {
            //pass
        }

        return newStudentQuiz;
    }

    private void createStatistics(Student student, StudentQuizRequestDto requestDto, Quiz quiz,
                                  StudentQuiz newStudentQuiz) {
        QuizStatistics quizStatistics = quizStatisticsRepository.findByStudentIdAndLectureId(
                        student.getId(), quiz.getLecture().getId())
                .orElseGet(() -> new QuizStatistics(
                        newStudentQuiz.getQuiz().getLecture().getId(), newStudentQuiz.getStudent().getId()));
        quizStatistics.update(requestDto.score());

        quizStatisticsRepository.save(quizStatistics);
    }
}
