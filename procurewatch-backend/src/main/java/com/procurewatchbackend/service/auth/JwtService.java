package com.procurewatchbackend.service.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.procurewatchbackend.model.entity.AppUser;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String SECRET = "procurewatch-simple-secret-key-change-later-123456789";
    private static final long EXPIRATION_SECONDS = 24 * 60 * 60;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateToken(AppUser user) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.getUsername());
            payload.put("email", user.getEmail());
            payload.put("role", user.getRole().name());
            payload.put("exp", Instant.now().getEpochSecond() + EXPIRATION_SECONDS);

            String headerPart = base64Url(objectMapper.writeValueAsBytes(header));
            String payloadPart = base64Url(objectMapper.writeValueAsBytes(payload));
            String unsignedToken = headerPart + "." + payloadPart;
            String signature = sign(unsignedToken);

            return unsignedToken + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Could not generate JWT token", e);
        }
    }

    public String extractUsername(String token) {
        return readPayload(token).get("sub").toString();
    }

    public String extractRole(String token) {
        return readPayload(token).get("role").toString();
    }

    public boolean isTokenValid(String token) {
        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                return false;
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);

            if (!expectedSignature.equals(parts[2])) {
                return false;
            }

            Map<String, Object> payload = readPayload(token);
            long expiration = Long.parseLong(payload.get("exp").toString());

            return expiration > Instant.now().getEpochSecond();
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> readPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);

            return objectMapper.readValue(payloadBytes, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    SECRET.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(keySpec);

            return base64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Could not sign JWT token", e);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}