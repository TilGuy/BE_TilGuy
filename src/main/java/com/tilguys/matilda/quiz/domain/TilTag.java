package com.tilguys.matilda.quiz.domain;

import lombok.Getter;

public class TilTag {

    @Getter
    private final String question;
    private final OXQuiz oxQuizAnswer;

    public TilTag(String question, OXQuiz oxQuizAnswer) {
        this.question = question;
        this.oxQuizAnswer = oxQuizAnswer;
    }

    public boolean isCorrect(OXQuiz userAnswer) {
        return userAnswer.equals(oxQuizAnswer);
    }
}
