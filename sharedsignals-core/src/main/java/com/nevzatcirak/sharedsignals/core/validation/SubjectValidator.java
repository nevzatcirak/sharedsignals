package com.nevzatcirak.sharedsignals.core.validation;

import com.nevzatcirak.sharedsignals.api.exception.SsfBadRequestException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates Subject JSON structure according to OpenID CAEP 1.0 / SSF 1.0 Specs.
 * <p>
 * Enforces strict rules for Simple, Complex, and Aliases formats.
 * </p>
 */
public class SubjectValidator {

    private static final Pattern FORMAT_NAME_PATTERN = Pattern.compile("^[a-z0-9_-]{1,20}$");

    /**
     * Validates a Subject JSON object.
     *
     * @param subject The Subject Identifier Map.
     */
    public static void validate(Map<String, Object> subject) {
        if (subject == null || subject.isEmpty()) {
            throw new SsfBadRequestException("Subject cannot be empty.");
        }

        String format = (String) subject.get("format");

        if (format == null || format.isBlank()) {
            throw new SsfBadRequestException("Subject must contain a 'format' field.");
        }

        if (!FORMAT_NAME_PATTERN.matcher(format).matches()) {
            throw new SsfBadRequestException(
                "Invalid format name: '" + format + "'."
            );
        }

        switch (format) {
            case "email":
                requireField(subject, "email", format);
                break;
            case "iss_sub":
                requireField(subject, "iss", format);
                requireField(subject, "sub", format);
                break;
            case "opaque":
                requireField(subject, "id", format);
                break;
            case "phone_number":
                requireField(subject, "phone_number", format);
                break;
            case "account":
                requireField(subject, "uri", format);
                break;
            case "did":
                requireField(subject, "url", format);
                break;
            case "uri":
                requireField(subject, "uri", format);
                break;
            case "jwt_id":
                requireField(subject, "iss", format);
                requireField(subject, "jti", format);
                break;
            case "saml_assertion_id":
                requireField(subject, "iss", format);
                requireField(subject, "saml_assertion_id", format);
                break;
            case "ip":
                requireField(subject, "ip", format);
                break;

            // --- NESTED TYPES ---
            case "aliases":
                validateAliases(subject);
                break;
            case "complex":
                validateComplex(subject);
                break;

            default:
                // Allow proprietary formats if they pass regex check.
                break;
        }
    }

    private static void requireField(Map<String, Object> subject, String fieldName, String format) {
        if (!subject.containsKey(fieldName) || subject.get(fieldName) == null) {
            throw new SsfBadRequestException(
                String.format("Subject format '%s' requires a '%s' field.", format, fieldName)
            );
        }
        Object value = subject.get(fieldName);
        if (value instanceof String && ((String) value).isBlank()) {
             throw new SsfBadRequestException(
                String.format("Field '%s' in format '%s' cannot be empty.", fieldName, format)
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static void validateAliases(Map<String, Object> subject) {
        if (!subject.containsKey("identifiers")) {
            throw new SsfBadRequestException("Subject format 'aliases' requires an 'identifiers' field.");
        }
        Object listObj = subject.get("identifiers");
        if (!(listObj instanceof List)) {
            throw new SsfBadRequestException("Field 'identifiers' must be a List.");
        }
        List<Object> list = (List<Object>) listObj;
        if (list.isEmpty()) {
            throw new SsfBadRequestException("Aliases 'identifiers' list cannot be empty.");
        }

        for (Object item : list) {
            if (item instanceof Map) {
                validate((Map<String, Object>) item);
            } else {
                throw new SsfBadRequestException("Items in 'identifiers' must be JSON Objects.");
            }
        }
    }

    /**
     * Validates the 'complex' format (SSF 3.3).
     * Rules:
     * 1. Must contain at least one member besides 'format'.
     * 2. Members must be valid Subject Identifiers.
     * 3. Members SHOULD be Simple Subject Members (No nested complex).
     */
    @SuppressWarnings("unchecked")
    private static void validateComplex(Map<String, Object> subject) {
        // Rule 1: At least one member + format = size >= 2
        if (subject.size() < 2) {
            throw new SsfBadRequestException("Complex subject must contain at least one member.");
        }

        for (Map.Entry<String, Object> entry : subject.entrySet()) {
            if ("format".equals(entry.getKey())) continue;

            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String, Object> memberSubject = (Map<String, Object>) value;

                // Rule 3: Check for nested complex (Avoid recursion loops and adhere to 'Simple Subject Member' hint)
                if ("complex".equals(memberSubject.get("format"))) {
                    throw new SsfBadRequestException(
                        "Nested 'complex' subjects are not allowed within a complex subject."
                    );
                }

                // Recursive validation for the member
                validate(memberSubject);

            } else {
                throw new SsfBadRequestException(
                    "Field '" + entry.getKey() + "' in 'complex' format must be a Subject Identifier Object."
                );
            }
        }
    }
}
