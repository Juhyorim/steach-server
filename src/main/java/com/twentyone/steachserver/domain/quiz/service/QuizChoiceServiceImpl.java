package com.twentyone.steachserver.domain.quiz.service;

import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.model.QuizChoice;
import com.twentyone.steachserver.domain.quiz.repository.QuizChoiceRepository;
import com.twentyone.steachserver.domain.quiz.validator.QuizChoiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuizChoiceServiceImpl implements QuizChoiceService{
    private final QuizChoiceRepository quizChoiceRepository;
    private final QuizChoiceValidator quizChoiceValidator;


    @Override
    @Transactional
    public void createQuizChoices(List<String> choices, String answers, Quiz savedQuiz){
        quizChoiceValidator.validateQuizChoices(choices, answers);

        int answerCount = 0;
        for (String choice : choices) {
            boolean isAnswer = answers.contains(choice);
            if (isAnswer) answerCount++;

            QuizChoice quizChoice = QuizChoice.createQuizChoice(choice, savedQuiz, isAnswer);
            quizChoiceRepository.save(quizChoice);
        }

        quizChoiceValidator.validateRightAnswers(answerCount);
    }


    public String getAnswers(Quiz quiz) {
        List<String> answers = quiz.getQuizChoices().stream()
                .filter(QuizChoice::getIsAnswer)
                .map(QuizChoice::getChoiceSentence)
                .collect(Collectors.toList());

        quizChoiceValidator.validateEmptyList(answers, "Answers cannot be empty");

        return answers.get(0);
    }

    public List<String> getChoices(Quiz quiz) {
        List<String> choices = quiz.getQuizChoices().stream()
                .map(QuizChoice::getChoiceSentence)
                .collect(Collectors.toList());

        quizChoiceValidator.validateEmptyList(choices, "Choices cannot be empty");

        return choices;
    }

    @Override
    @Transactional
    public void deleteChoice(QuizChoice quizChoice) {
        quizChoiceRepository.delete(quizChoice);
    }
}
