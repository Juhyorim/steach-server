package com.twentyone.steachserver.domain.studentQuiz.dto;

import lombok.Builder;

@Builder
public record StudentQuizRequestDtoV2(Integer score, Integer quizChoiceId) {

}
