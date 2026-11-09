package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACVerifier;
import privateCryptographicKeys.CryptographicKeys;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class JWTSupport {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private JWTSupport() { }

    static String readToken(HttpServletRequest request) {
        String token = request.getHeader("jwt");
        if (token == null || token.trim().isEmpty()) {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7))
                token = authorization.substring(7).trim();
        }
        return token;
    }

    static String validate(String jwtString) {
        if (jwtString == null || jwtString.trim().isEmpty()) return null;
        try {
            String unverifiedEmail = readUnverifiedSubject(jwtString);
            String password = CryptographicKeys.getUserJWTPassword(unverifiedEmail);
            if (password == null) return null;
            Verifier verifier = HMACVerifier.newVerifier(password);
            JWT jwt = JWT.getDecoder().decode(jwtString, verifier);
            return unverifiedEmail.equals(jwt.subject) ? jwt.subject : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static String readUnverifiedSubject(String jwtString) throws Exception {
        String[] parts = jwtString.split("\\.");
        if (parts.length != 3) return null;
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        JsonNode json = OBJECT_MAPPER.readTree(new String(payload, StandardCharsets.UTF_8));
        JsonNode subject = json.get("sub");
        return subject == null ? null : subject.asText();
    }
}
