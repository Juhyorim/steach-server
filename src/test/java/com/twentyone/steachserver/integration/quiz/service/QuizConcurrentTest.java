package com.twentyone.steachserver.integration.quiz.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;


import com.twentyone.steachserver.domain.curriculum.enums.CurriculumCategory;
import com.twentyone.steachserver.domain.curriculum.model.Curriculum;
import com.twentyone.steachserver.domain.curriculum.model.CurriculumDetail;
import com.twentyone.steachserver.domain.curriculum.repository.CurriculumDetailRepository;
import com.twentyone.steachserver.domain.curriculum.repository.CurriculumRepository;
import com.twentyone.steachserver.domain.lecture.model.Lecture;
import com.twentyone.steachserver.domain.lecture.repository.LectureRepository;
import com.twentyone.steachserver.domain.member.model.Student;
import com.twentyone.steachserver.domain.member.model.Teacher;
import com.twentyone.steachserver.domain.member.repository.StudentRepository;
import com.twentyone.steachserver.domain.member.repository.TeacherRepository;
import com.twentyone.steachserver.domain.quiz.dto.QuizChoiceRequest;
import com.twentyone.steachserver.domain.quiz.dto.QuizListRequestDto;
import com.twentyone.steachserver.domain.quiz.dto.QuizListResponseDto;
import com.twentyone.steachserver.domain.quiz.dto.QuizRequestDto;
import com.twentyone.steachserver.domain.quiz.dto.QuizRequestDtoV2;
import com.twentyone.steachserver.domain.quiz.dto.QuizResponseDto;
import com.twentyone.steachserver.domain.quiz.dto.QuizStatisticDto;
import com.twentyone.steachserver.domain.quiz.dto.QuizStatisticDtoV2;
import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.model.QuizChoice;
import com.twentyone.steachserver.domain.quiz.service.QuizRedisService;
import com.twentyone.steachserver.domain.quiz.service.QuizService;
import com.twentyone.steachserver.domain.studentQuiz.dto.StudentQuizRequestDtoV2;
import com.twentyone.steachserver.domain.studentQuiz.service.StudentQuizService;
import com.twentyone.steachserver.domain.studentQuiz.service.StudentQuizServiceImpl;
import com.twentyone.steachserver.integration.IntegrationTest;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@DisplayName("퀴즈 동시성 문제 관련 테스트")
public class QuizConcurrentTest extends IntegrationTest {
    public static final String CHOICE1 = "asdf";
    public static final String CHOICE2 = "qwer";
    public static final int QUIZ_NUMBER = 1;

    @Autowired
    QuizService quizService;
    @Autowired
    QuizRedisService quizRedisService;
    @Autowired
    StudentQuizService studentQuizService;
    @Autowired
    LectureRepository lectureRepository;
    @Autowired
    TeacherRepository teacherRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    StudentRepository quizRepository;
    @Autowired
    private CurriculumDetailRepository curriculumDetailRepository;
    @Autowired
    private CurriculumRepository curriculumRepository;

    private Teacher teacher;
    private Student student1;
    private Student student2;
    private Student student3;
    private Lecture lecture;
    private Curriculum curriculum;
    private Quiz quiz;
    private CurriculumDetail curriculumDetail;

    @BeforeEach
    void setUp() throws IOException {
        teacher = Teacher.of("teacher2", "password1", "김범식1", "bumsik1@gmail.com", "imagePath");
        student1 = Student.of("student1", "password1", "주효림1", "mylime1@gmail.com");
        student2 = Student.of("student2", "password2", "주효림2", "mylime2@gmail.com");
        student3 = Student.of("student3", "password3", "주효림3", "mylime3@gmail.com");

        teacherRepository.save(teacher);
        studentRepository.save(student1);
        studentRepository.save(student2);
        studentRepository.save(student3);

        //커리큘럼 생성
        curriculumDetail = CurriculumDetail.builder()
                .subTitle("title")
                .intro("subTitle")
                .subCategory("intro")
                .information("information")
                .bannerImgUrl("bannerImgUrl")
                .weekdaysBitmask((byte) 7)
                .startDate(LocalDate.from(LocalDate.now()))
                .endDate(LocalDate.from(LocalDate.now().plusWeeks(2)))
                .lectureStartTime(LocalTime.now())
                .lectureCloseTime(LocalTime.now())
                .maxAttendees(4)
                .build();
        curriculumDetailRepository.save(curriculumDetail);
        curriculum = Curriculum.of("title", CurriculumCategory.getCategoryByIndex(0), teacher, curriculumDetail);
        curriculumRepository.save(curriculum);
        lecture = Lecture.of("ㅁㄴㅇㄹ", 1, LocalDateTime.now(), LocalDateTime.now().plusHours(2), curriculum);
        lectureRepository.save(lecture);

        //퀴즈 생성
        List<QuizChoiceRequest> quizRequestDtoList = new ArrayList<>();
        quizRequestDtoList.add(new QuizChoiceRequest("SRP", false));
        quizRequestDtoList.add(new QuizChoiceRequest("DIP", false));
        quizRequestDtoList.add(new QuizChoiceRequest("LSP", false));
        quizRequestDtoList.add(new QuizChoiceRequest("OCP", false));
        quizRequestDtoList.add(new QuizChoiceRequest("RIP", true));

        QuizRequestDtoV2 quizListRequestDto = new QuizRequestDtoV2(1, "SOLID가 아닌것은?", 5, quizRequestDtoList);

        //when //then
        quizService.createQuizV2(teacher, lecture.getId(), quizListRequestDto);
        lecture = lectureRepository.findByCurriculumId(curriculum.getId()).get().get(0);
        quiz = lecture.getQuizzes().get(0);
    }

    @Test
    void 동시에_퀴즈를_풀고_답을_받아옴() throws Exception {
        //given
        List<QuizChoice> quizChoices = quiz.getQuizChoices();

        //when
        quiz.start();
        quizRedisService.initialize(quiz.getLecture(), quiz); //퀴즈 시작
        studentQuizService.createStudentQuizV2(student1, quiz.getId(), new StudentQuizRequestDtoV2(20, quizChoices.get(0).getId()));
        studentQuizService.createStudentQuizV2(student2, quiz.getId(), new StudentQuizRequestDtoV2(30, quizChoices.get(0).getId()));
        studentQuizService.createStudentQuizV2(student3, quiz.getId(), new StudentQuizRequestDtoV2(40, quizChoices.get(0).getId()));

        //then - 3명이 받아와도 같은 데이터가 나오는지 확인, 퀴즈점수 반영 전 조회되지 않는지 확인
        for (int i =0; i<3; i++) {
            QuizStatisticDtoV2 statisticsV2 = quizService.getStatisticsV2(quiz.getId());
            assertEquals(statisticsV2.getStatistics().get(0).getCount(), 3); //3명이 선택함

            assertEquals(statisticsV2.getCurrent().size(), 3);
            assertEquals(statisticsV2.getCurrent().get(0).getScore(), 40);
            assertEquals(statisticsV2.getCurrent().get(1).getScore(), 30);
            assertEquals(statisticsV2.getCurrent().get(2).getScore(), 20);

            assertEquals(student3.getName(), statisticsV2.getCurrent().get(0).getName());
            assertEquals(student2.getName(), statisticsV2.getCurrent().get(1).getName());
            assertEquals(student1.getName(), statisticsV2.getCurrent().get(2).getName());
        }
    }
}
