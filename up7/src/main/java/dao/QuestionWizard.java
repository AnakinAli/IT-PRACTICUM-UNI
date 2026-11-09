package dao;

import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import java.io.Serializable;
import java.util.ArrayList;

@Stateful
@LocalBean
public class QuestionWizard implements Serializable {
    private static final long serialVersionUID = 1L;

    private int nextQuestionNumber;
    private final ArrayList<String> userAnswers = new ArrayList<>();

    public String getNextQestion() {
        String question = Database.getQuestion(nextQuestionNumber);
        if (question != null) {
            nextQuestionNumber++;
        }
        return question;
    }

    public void saveAnswer(String answer) {
        userAnswers.add(answer == null ? "" : answer.trim());
    }

    @Remove
    public void sendAnswersToDatabase() {
        Database.saveAnwers(new ArrayList<>(userAnswers));
    }

    @PreDestroy
    private void destroy() {
        userAnswers.clear();
    }
}
