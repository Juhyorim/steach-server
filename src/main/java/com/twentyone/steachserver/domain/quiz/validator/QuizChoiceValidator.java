package com.twentyone.steachserver.domain.quiz.validator;

import com.twentyone.steachserver.domain.quiz.model.Quiz;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuizChoiceValidator {

    public void validateQuizChoices(List<String> choices, int answers) {
        validateNull(choices, "Choices");
        validateEmptyList(choices, "Choices cannot be empty");

        //퀴즈 정답 인덱스 유효성 검사
        if (answers>= choices.size() || answers < 0) {
            throw new IllegalArgumentException("정답관련 인덱스가 유효하지 않습니다.");
        }
    }

    private void validateNull(List<String> list, String name) {
        if (list == null) {
            throw new NullPointerException(name + " cannot be null");
        }
    }

    private void validateNull(String word, String name) {
        if (word == null) {
            throw new NullPointerException(name + " cannot be null");
        }
    }

    public void validateEmptyList(List<String> list, String Answers_cannot_be_empty) {
        if (list.isEmpty()) {
            throw new NullPointerException(Answers_cannot_be_empty);
        }
    }

    public void validateQuizAnswer(Quiz quiz) {

    }
}
