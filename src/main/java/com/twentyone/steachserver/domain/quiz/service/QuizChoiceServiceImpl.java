package com.twentyone.steachserver.domain.quiz.service;

import com.twentyone.steachserver.domain.quiz.dto.QuizChoiceRequest;
import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.model.QuizChoice;
import com.twentyone.steachserver.domain.quiz.repository.QuizChoiceRepository;
import com.twentyone.steachserver.domain.quiz.validator.QuizChoiceValidator;
import java.util.ArrayList;
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
    public List<QuizChoice> createQuizChoices(List<String> choices, int answer, Quiz savedQuiz){
        List<QuizChoice> quizChoiceList = new ArrayList<>();

        quizChoiceValidator.validateQuizChoices(choices, answer); //TODO 수정

        for (int i =0; i< choices.size(); i++) {
            String choice = choices.get(i);
            boolean isAnswer = (answer == i);

            QuizChoice quizChoice = QuizChoice.createQuizChoice(choice, savedQuiz, isAnswer);
            quizChoiceRepository.save(quizChoice);
            quizChoiceList.add(quizChoice);
        }

        return quizChoiceList;
    }

    @Override
    public List<QuizChoice> createQuizChoices(List<QuizChoiceRequest> quizChoiceList, Quiz quiz) {
        List<QuizChoice> quizChoices = new ArrayList<>();

        for (QuizChoiceRequest quizChoiceDto: quizChoiceList) {
            quizChoices.add(QuizChoice.createQuizChoice(quizChoiceDto.sentence(), quiz, quizChoiceDto.isAnswer()));
        }

        quizChoiceRepository.saveAll(quizChoices);
        return quizChoices;
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
