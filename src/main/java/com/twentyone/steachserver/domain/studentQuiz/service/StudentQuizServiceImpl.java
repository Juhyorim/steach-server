package com.twentyone.steachserver.domain.studentQuiz.service;

import com.twentyone.steachserver.domain.member.model.Student;
import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.model.QuizChoice;
import com.twentyone.steachserver.domain.quiz.model.QuizStatistics;
import com.twentyone.steachserver.domain.quiz.repository.QuizChoiceRepository;
import com.twentyone.steachserver.domain.quiz.repository.QuizRepository;
import com.twentyone.steachserver.domain.quiz.repository.QuizStatisticsRepository;
import com.twentyone.steachserver.domain.quiz.service.QuizRedisService;
import com.twentyone.steachserver.domain.studentQuiz.dto.StudentQuizRequestDto;
import com.twentyone.steachserver.domain.studentQuiz.dto.StudentQuizRequestDtoV2;
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
    private final QuizChoiceRepository quizChoiceRepository;
    private final QuizRedisService quizRedisService;

    @Secured("ROLE_STUDENT")
    @Transactional //TODO 삭제
    public StudentQuiz createStudentQuiz(Student student, Integer quizId, StudentQuizRequestDto requestDto) {
        Quiz quiz = getQuiz(quizId);

        validateStudentQuizIsUnique(student, quizId);

        StudentQuiz newStudentQuiz = StudentQuiz.createStudentQuiz(student, quiz, requestDto.score(),
                requestDto.studentChoice());
        studentQuizzesRepository.save(newStudentQuiz);

        //통계생성
        createStatistics(student, requestDto.score(), quiz, newStudentQuiz);

        return newStudentQuiz;
    }

    @Override
    @Secured("ROLE_STUDENT")
    @Transactional
    public StudentQuiz createStudentQuizV2(Student student, Integer quizId, StudentQuizRequestDtoV2 requestDto) {
        Quiz quiz = getQuiz(quizId);

        validateStudentQuizIsUnique(student, quizId);
        QuizChoice quizChoice = quizChoiceRepository.findById(requestDto.quizChoiceId())
                .orElseThrow(() -> new IllegalArgumentException("퀴즈 선택지 정보가 올바르지 않습니다."));

        StudentQuiz newStudentQuiz = StudentQuiz.createStudentQuiz(student, quiz, requestDto.score(),
                requestDto.quizChoiceId());
        studentQuizzesRepository.save(newStudentQuiz);

        //통계생성: Redis 사용, MySQL 로직 완료 후 마지막에 배치
        createStatisticsV2(student, quiz, newStudentQuiz, quizChoice);

        return newStudentQuiz;
    }

    private void validateStudentQuizIsUnique(Student student, Integer quizId) {
        Optional<StudentQuiz> studentQuiz = studentQuizzesRepository.findById(
                StudentQuizId.createStudentQuizId(student.getId(), quizId));
        if (studentQuiz.isPresent()) {
            throw new IllegalArgumentException("이미 점수가 존재합니다.");
        }
    }

    private Quiz getQuiz(Integer quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈입니다."));
    }

    private void createStatistics(Student student, Integer score, Quiz quiz,
                                  StudentQuiz newStudentQuiz) {
        //통계생성 - 실패해도 계속 진행하도록 처리
        try {
            QuizStatistics quizStatistics = quizStatisticsRepository
                    .findByStudentIdAndLectureId(student.getId(), quiz.getLecture().getId())
                    .orElseGet(() -> new QuizStatistics(newStudentQuiz.getQuiz().getLecture().getId(),
                            newStudentQuiz.getStudent().getId())
                    );

            quizStatistics.update(score);

            quizStatisticsRepository.save(quizStatistics);
        } catch (RuntimeException e) {
            //pass
        }
    }

    //Redis 버전
    private void createStatisticsV2(Student student, Quiz quiz,
                                  StudentQuiz newStudentQuiz, QuizChoice quizChoice) {
        //통계생성 - 실패해도 계속 진행하도록 처리
        try {
            quizRedisService.updateUserQuizScore(quiz.getLecture(), student, newStudentQuiz.getScore()); //랭킹 점수 갱신
            quizRedisService.updateQuizChoiceCount(quiz, quizChoice); //선택지 카운트 증가
        } catch (RuntimeException e) {
            //pass
        }
    }
}
