package dao;

import java.util.ArrayList;
import java.util.HashMap;

/** In-memory structures used in the exercise instead of a real database. */
public final class Database {
    public static final ArrayList<String> questions = new ArrayList<>();
    public static final HashMap<String, ArrayList<String>> answers = new HashMap<>();

    static {
        questions.add("Въведете своя имейл адрес:");
        questions.add("Кой е любимият Ви програмен език?");
        questions.add("Колко години опит в програмирането имате?");
        questions.add("Коя Java технология Ви е най-интересна?");
    }

    private Database() {
    }

    public static synchronized String getQuestion(int questionNumber) {
        if (questionNumber < 0 || questionNumber >= questions.size()) {
            return null;
        }
        return questions.get(questionNumber);
    }

    /**
     * The misspelled method name is retained because it is required by the
     * assignment. The first element is the email and becomes the map key.
     */
    public static synchronized void saveAnwers(ArrayList<String> userAnswers) {
        if (userAnswers == null || userAnswers.isEmpty()) {
            return;
        }

        String email = userAnswers.get(0);
        ArrayList<String> questionnaireAnswers =
                new ArrayList<>(userAnswers.subList(1, userAnswers.size()));
        answers.put(email, questionnaireAnswers);
    }

    public static void saveAnswers(ArrayList<String> userAnswers) {
        saveAnwers(userAnswers);
    }
}
