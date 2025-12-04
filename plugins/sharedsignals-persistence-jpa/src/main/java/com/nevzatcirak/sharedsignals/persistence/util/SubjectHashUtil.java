package com.nevzatcirak.sharedsignals.persistence.util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

/**
 * Utility for hashing subject JSONs to enable efficient database lookups.
 */
@Component
public class SubjectHashUtil {
    private final ObjectMapper objectMapper;
    public SubjectHashUtil() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        this.objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }
    public String computeHash(Map<String, Object> subject) {
        try {
            String json = objectMapper.writeValueAsString(subject);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    public String serialize(Map<String, Object> subject) {
        try { return objectMapper.writeValueAsString(subject); } catch (Exception e) { throw new RuntimeException(e); }
    }
}
