package com.twentyone.steachserver.domain.quiz.dto;

import com.twentyone.steachserver.domain.quiz.model.QuizChoice;
import java.util.ArrayList;
import java.util.List;

public record QuizChoiceDto(Integer quizChoiceId, String sentence, boolean isAnswer) {
    public static List<QuizChoiceDto> fromDomain(List<QuizChoice> quizChoices) {
        List<QuizChoiceDto> quizChoiceDtos = new ArrayList<>();
        for (QuizChoice quizChoice : quizChoices) {
            quizChoiceDtos.add(
                    new QuizChoiceDto(quizChoice.getId(), quizChoice.getChoiceSentence(), quizChoice.getIsAnswer()));
        }

        return quizChoiceDtos;
    }
}
