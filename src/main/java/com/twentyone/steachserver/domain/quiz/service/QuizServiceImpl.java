package com.twentyone.steachserver.domain.quiz.service;

import com.twentyone.steachserver.domain.auth.error.ForbiddenException;
import com.twentyone.steachserver.domain.lecture.repository.LectureRepository;
import com.twentyone.steachserver.domain.member.model.Teacher;
import com.twentyone.steachserver.domain.quiz.dto.*;
import com.twentyone.steachserver.domain.quiz.model.QuizChoice;
import com.twentyone.steachserver.domain.quiz.model.QuizStatistics;
import com.twentyone.steachserver.domain.quiz.repository.QuizStatisticsRepository;
import com.twentyone.steachserver.domain.quiz.validator.QuizChoiceValidator;
import com.twentyone.steachserver.domain.quiz.validator.QuizValidator;
import com.twentyone.steachserver.domain.lecture.model.Lecture;
import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.repository.QuizRepository;
import com.twentyone.steachserver.domain.studentQuiz.model.StudentQuiz;
import com.twentyone.steachserver.domain.studentQuiz.repository.StudentQuizRepository;
import com.twentyone.steachserver.global.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class  QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final LectureRepository lectureRepository;
    private final QuizChoiceService quizChoiceService;
    private final StudentQuizRepository studentQuizRepository;
    private final QuizChoiceValidator quizChoiceValidator;
//    private final QuizStatisticsRepository quizStatisticsRepository;
//    private final QuizValidator quizValidator;

    @Override
    @Transactional
    public QuizResponseDto createOneQuiz(Teacher teacher, Integer lectureId, QuizRequestDto request) {
        Lecture lecture = getLecture(lectureId);
        Quiz quiz = createQuiz(lecture, request);

        return new QuizResponseDto(
                quiz.getId(),
                quiz.getLecture().getId(),
                quiz.getQuizNumber(),
                quiz.getTime(),
                quiz.getQuestion(),
                quiz.getQuizChoiceString(),
                quiz.getAnswer()+1  //클라이언트에는 +1해서 주기
        );
    }

    @Override
    @Transactional
    public QuizListResponseDto createQuizList(Integer lectureId, QuizListRequestDto request) throws RuntimeException {
        Lecture lecture = getLecture(lectureId);

        List<Quiz> quizList = new ArrayList<>();

        int errorIdx = 1;
        for (QuizRequestDto quizRequestDto: request.quizList()) {
            try {
                Quiz quiz = createQuiz(lecture, quizRequestDto);
                quizList.add(quiz);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException(errorIdx+"번째 퀴즈에 오류발생: " + e.getMessage());
            }

            errorIdx++;
        }

        return QuizListResponseDto.fromDomainList(quizList);
    }

    @Transactional
    public Quiz createQuiz(Lecture lecture, QuizRequestDto quizRequestDto) {
        //유효성 검사
        quizChoiceValidator.validateQuizChoices(quizRequestDto.getChoices(), quizRequestDto.getAnswers());

        //퀴즈 생성
        Quiz quiz = Quiz.createQuiz(
                quizRequestDto.getQuestion(),
                quizRequestDto.getQuizNumber(),
                quizRequestDto.getTime(),
                lecture
        );
        quiz = quizRepository.save(quiz);

        //QuizChoice 생성
        List<String> choices = quizRequestDto.getChoices();
        List<QuizChoice> quizChoices = quizChoiceService.createQuizChoices(choices, quizRequestDto.getAnswers()-1, quiz);
        quiz.addChoiceList(quizChoices);

        return quiz;
    }

    private Lecture getLecture(Integer lectureId) {
        Optional<Lecture> lectureOpt = lectureRepository.findById(lectureId);

        if (lectureOpt.isEmpty()) {
            throw new IllegalArgumentException("Lecture not found");
        }

        return lectureOpt.get();
    }

    @Override
    public Optional<Quiz> findById(Integer quizId) {
        return quizRepository.findById(quizId);
    }

    @Override
    public QuizResponseDto mapToDto(Quiz quiz) {
        List<String> choices = quizChoiceService.getChoices(quiz);

        return QuizResponseDto.createQuizResponseDto(quiz, choices, quiz.getAnswer());
    }

    @Override
    public List<Quiz> findAllByLectureId(Integer lectureId) {
        return quizRepository.findALlByLectureId(lectureId);
    }

    @Override
    @Transactional
    public void delete(Integer quizId, Teacher teacher) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("퀴즈를 찾을 수 없음"));

        //퀴즈를 만든 사람인지 확인
        if (!quiz.getLecture().getCurriculum().getTeacher().equals(teacher)) {
            throw new ForbiddenException("퀴즈를 만든 사람만 삭제가 가능합니다.");
        }

        quizRepository.delete(quiz);
    }

    @Override
    @Transactional
    public QuizResponseDto modifyQuiz(Teacher teacher, Integer quizId, QuizRequestDto dto) {
        //dto 검증로직
        if (dto.getAnswers() == null && dto.getChoices() != null) {
            throw new IllegalArgumentException("quiz choices에 수정사항이 있다면 answer는 null이어서는 안됩니다.");
        }

        Quiz quiz = quizRepository.findByIdWithQuizChoice(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("퀴즈를 찾을 수 없음"));

        //퀴즈를 만든 사람만 수정가능
        Teacher makeTeacher = quiz.getLecture().getCurriculum().getTeacher();
        if (!teacher.equals(makeTeacher)) {
            throw new ForbiddenException("퀴즈를 수정할 권한이 없습니다");
        }

        //answer만 바뀌는 경우
        if (dto.getChoices() == null && dto.getAnswers() != null) {
            List<QuizChoice> quizChoices = quiz.getQuizChoices();
            int answerIndex = dto.getAnswers() - 1; //TODO 이건 어떻게 처리하지? 실수 발생할 것 같다..

            if (answerIndex >= quiz.getQuizChoices().size() || answerIndex < 0) {
                throw new IllegalArgumentException("정답관련 인덱스가 유효하지 않습니다.");
            }

            for (int i =0; i<quizChoices.size(); i++) {
                if (answerIndex == i) {
                    quizChoices.get(i).setIsAnswer();
                } else {
                    quizChoices.get(i).setIsNotAnswer();
                }
            }
        }

        //choice + answer 둘 다 바뀌는 경우
        if (dto.getChoices() != null && dto.getAnswers() != null) {
            List<QuizChoice> quizChoices = quiz.getQuizChoices();

            //TODO choice 도 몇 개 없는데, 그냥 삭제하는 게 나을까? 고민해보기..
            quiz.deleteAllQuizChoice();
            for (QuizChoice qc: quizChoices) {
                quizChoiceService.deleteChoice(qc);
            }

            int answerIndex = dto.getAnswers() - 1; //TODO 이건 어떻게 처리하지? 실수 발생할 것 같다..
            //클라이언트 인덱스 유효성 검사
            if (answerIndex >= dto.getChoices().size() || answerIndex < 0) {
                throw new IllegalArgumentException("정답관련 인덱스가 유효하지 않습니다.");
            }

            List<String> inputChoices = dto.getChoices();
            quizChoiceService.createQuizChoices(inputChoices, answerIndex, quiz);

            quiz.modify(dto.getQuizNumber(), dto.getQuestion());
        }

        return mapToDto(quiz);
    }

    @Override
    @Transactional
    public QuizListResponseDto modifyManyQuiz(Teacher teacher, Integer lectureId, QuizListRequestDto request) {
        Lecture lecture = lectureRepository.findByIdWithQuiz(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("찾을 수 없는 강의 id"));

        //관련 퀴즈 싹 삭제
        List<Quiz> quizzes = lecture.getQuizzes();
        for (Quiz quiz: quizzes) {
            quizRepository.delete(quiz);
        }

        QuizListResponseDto responseDto = createQuizList(lectureId, request);

        return responseDto;
    }

    @Override
    @Transactional
    public QuizStatisticDto getStatistics(Integer quizId) { //통계데이터 TODO redis로 변경
        /*
         * <나와야하는 결과물>
         * 1) 한 퀴즈에 대해 선택지 당 선택된 개수
         * 2) 한 강의에서 사람들의 current 퀴즈점수 + rank매기기
         */
        Quiz quiz = quizRepository.findById(quizId)
                        .orElseThrow(() -> new ResourceNotFoundException("찾을 수 없는 퀴즈"));

        //1) 한 퀴즈에 대해 선택지 당 선택된 개수
        List<StudentQuiz> studentQuizByQuiz = studentQuizRepository.findByQuiz(quiz);
        List<QuizChoice> quizChoices = quiz.getQuizChoices();
        int[] count = new int[quizChoices.size()];

        for (StudentQuiz studentQuiz: studentQuizByQuiz) {
            for (int i =0; i<quizChoices.size(); i++) {
                QuizChoice quizChoice = quizChoices.get(i);

                if (quizChoice.getChoiceSentence().equals(studentQuiz.getStudentChoice())) {
                    count[i] ++;
                    break;
                }
            }
        }

        // 2) 한 강의에서 사람들의 current 퀴즈점수 + rank매기기
        Lecture lecture = quiz.getLecture();
        List<Quiz> quizzes = lecture.getQuizzes();

        Map<String, Integer> scoreBoard = new HashMap<>();
        for (Quiz eachQuiz: quizzes) {
            List<StudentQuiz> studentQuizzes = eachQuiz.getStudentQuizzes();
            for (StudentQuiz studentQuiz: studentQuizzes) {
                String studentName = studentQuiz.getStudent().getName();
                Integer score = scoreBoard.getOrDefault(studentName, 0) + studentQuiz.getScore();
                scoreBoard.put(studentName, score);
            }
        }

        List<QuizStudentScoreDto> current = new ArrayList<>();
        for (String studentName: scoreBoard.keySet()) {
            current.add(new QuizStudentScoreDto(0, scoreBoard.get(studentName), studentName));
        }

        //score 총 합 기준으로 정렬
        Collections.sort(current, (o1, o2) -> o2.getScore() - o1.getScore());

        //랭크 부여
        int rank = 1;
        for (QuizStudentScoreDto dto: current) {
            dto.setCurrentRank(rank++);
        }

        List<Integer> statistics = Arrays.stream(count).boxed().collect(Collectors.toList());
        QuizStatisticDto quizStatisticDto = new QuizStatisticDto(statistics, new ArrayList<>(), current);

        return quizStatisticDto;
    }
}
