package com.twentyone.steachserver.domain.quiz.model;

import com.twentyone.steachserver.domain.studentQuiz.model.StudentQuiz;
import com.twentyone.steachserver.domain.quiz.dto.QuizRequestDto;
import com.twentyone.steachserver.domain.lecture.model.Lecture;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@NoArgsConstructor
@Getter(value = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PRIVATE)
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 강의의 몇번째 퀴즈인지
    @Column(columnDefinition = "TINYINT(4)")
    private Integer quizNumber;

    @Column(name = "question", nullable = false)
    private String question;
    
    private Integer time; //퀴즈 제한시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", referencedColumnName = "id")
    private Lecture lecture;

    @OneToMany(mappedBy = "quiz")
    private List<StudentQuiz> studentQuizzes = new ArrayList<>();

    @OneToMany(mappedBy = "quiz")
    private List<QuizChoice> quizChoices = new ArrayList<>();

    public static Quiz createQuiz(String question, Integer quizNumber, Integer time, Lecture lecture) {
        Quiz quiz = new Quiz();
        quiz.setLecture(lecture);
        quiz.setQuestion(question);
        quiz.setQuizNumber((quizNumber == null || quizNumber== 0)? lecture.getQuizzes().size() + 1 : quizNumber);
        quiz.setTime(time);

        lecture.addQuiz(quiz);
        return quiz;
    }

    public void addStudentQuiz(StudentQuiz studentQuiz) {
        this.getStudentQuizzes().add(studentQuiz);
        studentQuiz.updateQuiz(this);
    }

    public void addChoice(QuizChoice quizChoice) {
        this.getQuizChoices().add(quizChoice);
        quizChoice.updateQuiz(this);
    }

    public List<String> getQuizChoiceString() {
        List<String> choice = new ArrayList<>();

        for (QuizChoice quizChoice: this.quizChoices) {
            choice.add(quizChoice.getChoiceSentence());
        }

        return choice;
    }

    public Integer getAnswer() {
        int i = 0;
        for (QuizChoice quizChoice: this.quizChoices) {
            if (quizChoice.getIsAnswer()) {
                return i;
            }
            i++;
        }
        
        throw new RuntimeException("에러에러에러");
    }

    public void modify(Integer quizNumber, String question) {
        if (quizNumber != null)
            this.quizNumber = quizNumber;

        if (question != null)
            this.question = question;
        //        Quiz quiz = new Quiz();
        //        quiz.setLecture(lecture);
        //        quiz.setQuestion(request.question());
        //        quiz.setQuizNumber((request.quizNumber() == null || request.quizNumber()== 0)? lecture.getQuizzes().size() + 1 : request.quizNumber());
        //
        //        lecture.addQuiz(quiz);
        //        return quiz;
    }

    public void deleteAllQuizChoice() {
        this.quizChoices = new ArrayList<>();
    }

    public void addChoiceList(List<QuizChoice> quizChoices) {
        this.quizChoices = quizChoices;
    }
}
