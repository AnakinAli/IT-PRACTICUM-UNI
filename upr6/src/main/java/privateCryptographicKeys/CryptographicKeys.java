package privateCryptographicKeys;

import java.util.HashMap;
import java.util.UUID;

public final class CryptographicKeys {
    public static final String APP_PASSWORD = UUID.randomUUID().toString();
    private static final HashMap<String, String> userJWTPasswords = new HashMap<>();

    static {
        userJWTPasswords.put("ivan@example.com", UUID.randomUUID().toString());
        userJWTPasswords.put("maria@example.com", UUID.randomUUID().toString());
        userJWTPasswords.put("student@example.com", UUID.randomUUID().toString());
    }

    private CryptographicKeys() { }

    public static synchronized String getUserJWTPassword(String userEmail) {
        if (userEmail == null) return null;
        return userJWTPasswords.computeIfAbsent(userEmail, ignored -> UUID.randomUUID().toString());
    }

    public static synchronized void changeUserJWTPassword(String userEmail) {
        if (userEmail != null) userJWTPasswords.put(userEmail, UUID.randomUUID().toString());
    }
}
