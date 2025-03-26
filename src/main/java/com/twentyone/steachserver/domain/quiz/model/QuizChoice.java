package com.twentyone.steachserver.domain.quiz.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_choices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter(value = AccessLevel.PUBLIC)
public class QuizChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "is_answer", nullable = false)
    private Boolean isAnswer;

    @Column(name = "choice_sentence", nullable = false)
    private String choiceSentence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", referencedColumnName = "id")
    private Quiz quiz;

    public static QuizChoice createEmptyQuizChoice() {
        return new QuizChoice();
    }

    public static QuizChoice createQuizChoice(String choiceText, Quiz savedQuiz, boolean isAnswer) {
        QuizChoice quizChoice = new QuizChoice();
        quizChoice.choiceSentence = choiceText;
        quizChoice.quiz = savedQuiz;
        quizChoice.isAnswer = isAnswer;
        savedQuiz.addChoice(quizChoice);
        return quizChoice;
    }

    public void updateQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public void setIsNotAnswer() {
        this.isAnswer = false;
    }

    public void setIsAnswer() {
        this.isAnswer = true;
    }
}
