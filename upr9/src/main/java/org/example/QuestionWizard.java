package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;
import javax.enterprise.context.SessionScoped;

/** Stores one user's progress through the quiz. */
@Stateful
@StatefulTimeout(value = 5, unit = TimeUnit.SECONDS)
@SessionScoped
public class QuestionWizard implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String[] QUESTIONS = {
        "Кой език се изпълнява в Java Virtual Machine?",
        "Коя анотация указва stateful session bean?",
        "Кой HTTP метод обикновено се използва за изпращане на данни?"
    };

    private final List<String> answers = new ArrayList<>();

    public String getCurrentQuestion() {
        return isFinished() ? null : QUESTIONS[answers.size()];
    }

    public int getCurrentNumber() {
        return Math.min(answers.size() + 1, QUESTIONS.length);
    }

    public int getQuestionCount() {
        return QUESTIONS.length;
    }

    public List<String> getAnswers() {
        return Collections.unmodifiableList(new ArrayList<>(answers));
    }

    public boolean isFinished() {
        return answers.size() >= QUESTIONS.length;
    }

    public void answer(String answer) {
        if (!isFinished()) {
            answers.add(answer);
        }
    }

    @Remove
    public void reset() {
        System.out.println("@Remove");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("@PreDestroy");
    }
}
