package com.twentyone.steachserver.integration.quiz.service;

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
import com.twentyone.steachserver.domain.quiz.dto.QuizListRequestDto;
import com.twentyone.steachserver.domain.quiz.dto.QuizListResponseDto;
import com.twentyone.steachserver.domain.quiz.dto.QuizRequestDto;
import com.twentyone.steachserver.domain.quiz.dto.QuizResponseDto;
import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.model.QuizChoice;
import com.twentyone.steachserver.domain.quiz.repository.QuizRepository;
import java.time.LocalTime;

import com.twentyone.steachserver.domain.quiz.service.QuizService;
import com.twentyone.steachserver.integration.IntegrationTest;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("퀴즈 통합 테스트")
@Transactional
@SpringBootTest
public class QuizIntegrationTest {
    public static final String CHOICE1 = "asdf";
    public static final String CHOICE2 = "qwer";
    public static final int QUIZ_NUMBER = 1;

    @Autowired
    QuizService quizService;
    @Autowired
    LectureRepository lectureRepository;
    @Autowired
    TeacherRepository teacherRepository;
    @Autowired
    StudentRepository studentRepository;

    @Autowired
    QuizRepository quizRepository;

    @Autowired
    private CurriculumDetailRepository curriculumDetailRepository;
    @Autowired
    private CurriculumRepository curriculumRepository;

    private Teacher teacher;
    private Student student;
    private Lecture lecture;
    private Curriculum curriculum;
    private CurriculumDetail curriculumDetail;

    @BeforeEach
    void setUp() throws IOException {
        teacher = Teacher.of("teacher2", "password1", "김범식1", "bumsik1@gmail.com", "imagePath");
        student = Student.of("student2", "password2", "주효림1", "mylime1@gmail.com");

        teacherRepository.save(teacher);
        studentRepository.save(student);

        curriculumDetail = CurriculumDetail.builder()
                .subTitle("title")
                .intro("subTitle")
                .subCategory("intro")
                .information("information")
                .bannerImgUrl("bannerImgUrl")
                .weekdaysBitmask((byte) 7)
                .startDate(LocalDate.from(LocalDate.now()))
                .endDate(LocalDate.from(LocalDate.now()))
                .lectureStartTime(LocalTime.now())
                .lectureCloseTime(LocalTime.now())
                .maxAttendees(4)
                .build();
        curriculumDetailRepository.save(curriculumDetail);

        curriculum = Curriculum.of("title", CurriculumCategory.getCategoryByIndex(0), teacher, curriculumDetail);
        curriculumRepository.save(curriculum);
        lecture = lectureRepository.save(Lecture.of("title", 1, LocalDateTime.now(), LocalDateTime.now().plusHours(2), curriculum));
    }

    @Test
    @DisplayName("단일 퀴즈 생성")
    void createOneQuiz() {
        //given
        QuizRequestDto quizRequestDto = QuizRequestDto.of(1, "질문", List.of("답1", "답2"), 1, 5);

        //when
        QuizResponseDto oneQuiz = quizService.createOneQuiz(teacher, lecture.getId(), quizRequestDto);
        Quiz quiz = quizRepository.findById(oneQuiz.getQuizId()).get();

        //then
        //response와 비교
        assertEquals(quizRequestDto.getQuizNumber(), oneQuiz.getQuizNumber());
        assertEquals(quizRequestDto.getChoices(), oneQuiz.getChoices());
        assertEquals(quizRequestDto.getAnswers(), oneQuiz.getAnswers()); //request와 response는 차이가 나면 안됨
        assertEquals(quizRequestDto.getQuestion(), oneQuiz.getQuestion());
        assertEquals(quizRequestDto.getTime(), oneQuiz.getTime());

        //데이터베이스 값과 비교
        assertEquals(quiz.getQuizNumber(), oneQuiz.getQuizNumber());
        assertEquals(quiz.getQuizChoiceString(), oneQuiz.getChoices());
        assertEquals(quiz.getAnswer()+1, oneQuiz.getAnswers()); //데이터베이스에서의 answer와는 1 차이 나야함
        assertEquals(quiz.getQuestion(), oneQuiz.getQuestion());
        assertEquals(quiz.getTime(), oneQuiz.getTime());
    }

    @Test
    @DisplayName("퀴즈 여러 개 생성")
    void createManyQuiz() throws Exception {
        //given
        List<QuizRequestDto> quizRequestDtoList = new ArrayList<>();
        quizRequestDtoList.add(new QuizRequestDto(1, "마루는 귀엽다", List.of("O", "X"), 1, 1));
        quizRequestDtoList.add(new QuizRequestDto(2, "마루는 안귀엽다", List.of("O", "X"), 2, 1));
        quizRequestDtoList.add(new QuizRequestDto(3, "가장 귀여운 강아지 이름은?", List.of("핑핑이", "마루", "서브웨이"), 2, 1));

        QuizListRequestDto quizListRequestDto = new QuizListRequestDto(quizRequestDtoList);

        //when //then
        AtomicReference<QuizListResponseDto> quizList = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            quizList.set(quizService.createQuizList(lecture.getId(), quizListRequestDto));
        });

        List<QuizResponseDto> responseDtoList = quizList.get().quizList();
        assertEquals(quizRequestDtoList.size(), responseDtoList.size());

        for (int i=0; i<responseDtoList.size(); i++) {
            QuizResponseDto quizResponseDto = responseDtoList.get(i);
            QuizRequestDto quizRequestDto = quizRequestDtoList.get(i);

            assertEquals(quizRequestDto.getQuizNumber(), quizRequestDto.getQuizNumber());
            assertEquals(quizRequestDto.getQuestion(), quizRequestDto.getQuestion());
            assertEquals(quizRequestDto.getChoices().size(), quizRequestDto.getChoices().size());

            for (int j=0; j<quizRequestDto.getChoices().size(); j++) {
                assertEquals(quizRequestDto.getChoices().get(j), quizResponseDto.getChoices().get(j));
            }

            assertEquals(quizRequestDto.getAnswers(), quizRequestDto.getAnswers());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 3, 4, 5})
    void 퀴즈생성_answer_인덱스_에러(int answerIdx) throws Exception {
        //given
        List<QuizRequestDto> quizRequestDtoList = new ArrayList<>();
        quizRequestDtoList.add(new QuizRequestDto(1, "마루는 안귀엽다", List.of("O", "X"), 2, 1));
        quizRequestDtoList.add(new QuizRequestDto(2, "마루는 귀엽다", List.of("O", "X"), answerIdx, 1)); //에러발생!!!
        quizRequestDtoList.add(new QuizRequestDto(3, "가장 귀여운 강아지 이름은?", List.of("핑핑이", "마루", "서브웨이"), 2, 1));

        QuizListRequestDto quizListRequestDto = new QuizListRequestDto(quizRequestDtoList);

        //when //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            quizService.createQuizList(lecture.getId(), quizListRequestDto);
        });

        assertTrue(e.getMessage().contains("2번째"));
        assertTrue(e.getMessage().contains("정답관련 인덱스가 유효하지 않습니다"));
        log.info(e.getMessage());
    }

    @Test
    void 퀴즈조회() {
        //given
        Lecture createdLecture = lectureRepository.save(Lecture.of("title", 1, LocalDateTime.now(),LocalDateTime.now().plusHours(2), curriculum));
        String question = "asdf";

        List<String> choices = List.of(new String[]{CHOICE1, CHOICE2});
        Integer answer = 1; //CHOICE1

        List<QuizRequestDto> list = List.of(new QuizRequestDto(QUIZ_NUMBER, question, choices, answer, 1));

        QuizListResponseDto quizListResponse = quizService.createQuizList(createdLecture.getId(), new QuizListRequestDto(list));

        Quiz quiz = quizService.findById(quizListResponse.quizList().get(0).getQuizId())
                .orElseThrow(() -> new RuntimeException("asdf"));

        assertEquals(quiz.getQuizNumber(), QUIZ_NUMBER);
        assertEquals(quiz.getQuestion(), question);
        assertEquals(choices.size(), quiz.getQuizChoices().size());

        for (int i = 0; i < choices.size(); i++) {
            QuizChoice quizChoice = quiz.getQuizChoices().get(i);

            //정답 문장이 잘 들어가는지 확인
            assertEquals(choices.get(i), quizChoice.getChoiceSentence());

            //answer 여부가 잘 저장되는지 확인
            if (i+1 == answer) {
                assertTrue(quizChoice.getIsAnswer());
            } else {
                assertFalse(quizChoice.getIsAnswer());
            }
        }
    }
}
