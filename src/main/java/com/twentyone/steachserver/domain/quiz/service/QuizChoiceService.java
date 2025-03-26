package com.twentyone.steachserver.domain.quiz.service;

import com.twentyone.steachserver.domain.quiz.dto.QuizChoiceRequest;
import com.twentyone.steachserver.domain.quiz.model.Quiz;

import com.twentyone.steachserver.domain.quiz.model.QuizChoice;
import java.util.List;

public interface QuizChoiceService {
    List<QuizChoice> createQuizChoices(List<String> choices, int answers, Quiz savedQuiz);
    String getAnswers(Quiz quiz);
    List<String> getChoices(Quiz quiz);
    void deleteChoice(QuizChoice quizChoice);
    List<QuizChoice> createQuizChoices(List<QuizChoiceRequest> quizChoiceList, Quiz quiz);
}
