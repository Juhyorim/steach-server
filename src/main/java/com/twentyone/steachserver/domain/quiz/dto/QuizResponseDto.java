package com.twentyone.steachserver.domain.quiz.dto;

import com.twentyone.steachserver.domain.quiz.model.Quiz;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
public class QuizResponseDto {
    private Integer quizId;
    private Integer lectureId;
    private Integer quizNumber;
    private Integer time;
    private String question;
    private List<String> choices;
    private Integer answers;

    public static QuizResponseDto createQuizResponseDto(Quiz quiz, List<String> choices, Integer answers) {
        allStrip(choices);

        return new QuizResponseDto(
                quiz.getId(),
                quiz.getLecture().getId(),
                quiz.getQuizNumber(),
                quiz.getTime(),
                quiz.getQuestion(),
                choices,
                answers //클라이언트에는 +1해서 주기
        );
    }

    private static void allStrip(List<String> strings){
        strings.replaceAll(String::strip);
    }

    public static QuizResponseDto fromDomain(Quiz quiz) {
        return new QuizResponseDto(
                quiz.getId(),
                quiz.getLecture().getId(),
                quiz.getQuizNumber(),
                quiz.getTime(),
                quiz.getQuestion(),
                quiz.getQuizChoiceString(),
                quiz.getAnswer()+1  //클라이언트에는 +1해서 주기 //TODO
        );
    }
}
