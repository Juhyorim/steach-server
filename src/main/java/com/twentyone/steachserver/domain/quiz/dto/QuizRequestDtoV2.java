package com.twentyone.steachserver.domain.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuizRequestDtoV2 {
    private Integer quizNumber;
    private String question;
    @Schema(example = "5")
    private Integer time = 5; // default 5초
    public List<QuizChoiceRequest> quizChoiceList;
}
