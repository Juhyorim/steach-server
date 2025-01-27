package com.twentyone.steachserver.domain.quiz.dto;

import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.model.QuizChoice;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QuizResponseDtoV2 {
    private Integer quizId;
    private Integer lectureId;
    private Integer quizNumber;
    private Integer time;
    private String question;
    private List<QuizChoiceDto> choices;

    public static QuizResponseDtoV2 fromDomain(Quiz quiz) {
        List<QuizChoice> quizChoices = quiz.getQuizChoices();
        List<QuizChoiceDto> choices = QuizChoiceDto.fromDomain(quizChoices);

        return new QuizResponseDtoV2(
                quiz.getId(),
                quiz.getLecture().getId(),
                quiz.getQuizNumber(),
                quiz.getTime(),
                quiz.getQuestion(),
                choices
        );
    }
}
