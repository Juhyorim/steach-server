package com.twentyone.steachserver.domain.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 *   lectureId: 1,
 *   quizNumber : 1,
 *   question: “여행하면서 겪은 감상을 표현하는 글을 무엇이라 하는가?”,
 *   choice: [
 *     “기행문”, “소설”, “수필”, “시”
 *   ],
 *   answer: [
 *     “기행문”, “소설”
 *   ] // 중복가능
 * }
 */
@AllArgsConstructor //TODO 삭제
@NoArgsConstructor
public class QuizRequestDto {
    private Integer quizNumber;
    private String question;
    private List<String> choices;
    @Min(1)
    private Integer answers;
    @Schema(example = "5")
    private Integer time = 5; // default 5초

    public Integer getQuizNumber() {
        return quizNumber;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public Integer getAnswers() {
        return answers; //서버용
    }

    public Integer getTime() {
        return time;
    }

    public static QuizRequestDto of(Integer quizNumber, String question, List<String> choices, Integer answers, Integer time) {
        QuizRequestDto quizRequestDto = new QuizRequestDto();
        quizRequestDto.quizNumber = quizNumber;
        quizRequestDto.question = question;
        quizRequestDto.choices = choices;
        quizRequestDto.answers = answers;
        quizRequestDto.time = time;

        return quizRequestDto;
    }
}
