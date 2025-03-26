package com.twentyone.steachserver.domain.quiz.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class QuizStatisticDtoV2 {
    private List<QuizOptionsDto> statistics;
    private List<QuizStudentScoreDto> prev;
    private List<QuizStudentScoreDto> current;

    @Getter
    @AllArgsConstructor
    public static class QuizOptionsDto {
        private Integer quizChoiceId;
        private String sentence;
        private Integer count;
    }
}
