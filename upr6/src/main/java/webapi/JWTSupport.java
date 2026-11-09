package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = authorization.substring(7).trim();
            }
        }
        return token;
    }

    static String readUnverifiedSubject(String jwtString) throws Exception {
        String[] parts = jwtString.split("\\.");
        if (parts.length != 3) return null;
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        JsonNode json = OBJECT_MAPPER.readTree(new String(payload, StandardCharsets.UTF_8));
        JsonNode subject = json.get("sub");
        return subject == null ? null : subject.asText();
    }
}
