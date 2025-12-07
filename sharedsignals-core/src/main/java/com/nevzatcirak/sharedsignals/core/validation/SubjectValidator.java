package com.nevzatcirak.sharedsignals.core.validation;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.exception.SsfBadRequestException;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates Subject JSON structure according to OpenID CAEP 1.0 / SSF 1.0 Specs.
 * <p>
 * Enforces strict rules for Simple, Complex, and Aliases formats.
 * Includes protection against Nested JSON DoS attacks.
 * </p>
 */
public class SubjectValidator {

    private static final Pattern FORMAT_NAME_PATTERN = Pattern.compile("^[a-z0-9_-]{1,20}$");

    // SECURITY: Limit recursion depth to prevent StackOverflow/DoS attacks
    private static final int MAX_DEPTH = 5;

    public static void validate(Map<String, Object> subject) {
        validate(subject, 0);
    }

    private static void validate(Map<String, Object> subject, int depth) {
        if (depth > MAX_DEPTH) {
            throw new SsfBadRequestException("Subject nesting too deep (Max " + MAX_DEPTH + "). Possible DoS attempt.");
        }

        if (subject == null || subject.isEmpty()) {
            throw new SsfBadRequestException("Subject cannot be empty.");
        }

        String format = (String) subject.get("format");
        if (format == null || format.isBlank()) {
            throw new SsfBadRequestException("Subject must contain a 'format' field.");
        }

        if (!FORMAT_NAME_PATTERN.matcher(format).matches()) {
            throw new SsfBadRequestException("Invalid format name: '" + format + "'.");
        }

        switch (format) {
            case SharedSignalConstants.FORMAT_EMAIL:
                requireField(subject, "email", format);
                break;
            case SharedSignalConstants.FORMAT_ISSUER_SUBJECT:
                requireField(subject, "iss", format);
                requireField(subject, "sub", format);
                break;
            case SharedSignalConstants.FORMAT_OPAQUE:
                requireField(subject, "id", format);
                break;
            case SharedSignalConstants.FORMAT_PHONE:
                requireField(subject, "phone_number", format);
                break;
            case SharedSignalConstants.FORMAT_ACCOUNT:
                requireField(subject, "uri", format);
                break;
            case SharedSignalConstants.FORMAT_DID:
                requireField(subject, "url", format);
                break;
            case SharedSignalConstants.FORMAT_URI:
                requireField(subject, "uri", format);
                break;
            case SharedSignalConstants.FORMAT_JWT_ID:
                requireField(subject, "iss", format);
                requireField(subject, "jti", format);
                break;
            case SharedSignalConstants.FORMAT_SAML_ASSERTION_ID:
                requireField(subject, "iss", format);
                requireField(subject, "saml_assertion_id", format);
                break;
            case SharedSignalConstants.FORMAT_IP:
                requireField(subject, "ip", format);
                break;

            // --- NESTED TYPES ---
            case SharedSignalConstants.FORMAT_ALIASES:
                validateAliases(subject, depth);
                break;
            case SharedSignalConstants.FORMAT_COMPLEX:
                validateComplex(subject, depth);
                break;
            default:
                // Allow proprietary formats but ensure basic structure
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
    private static void validateAliases(Map<String, Object> subject, int depth) {
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
                validate((Map<String, Object>) item, depth + 1);
            } else {
                throw new SsfBadRequestException("Items in 'identifiers' must be JSON Objects.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void validateComplex(Map<String, Object> subject, int depth) {
        if (subject.size() < 2) {
            throw new SsfBadRequestException("Complex subject must contain at least one member.");
        }

        for (Map.Entry<String, Object> entry : subject.entrySet()) {
            if ("format".equals(entry.getKey())) continue;
            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String, Object> memberSubject = (Map<String, Object>) value;
                if (SharedSignalConstants.FORMAT_COMPLEX.equals(memberSubject.get("format"))) {
                    throw new SsfBadRequestException("Nested 'complex' subjects are not allowed within a complex subject.");
                }
                validate(memberSubject, depth + 1);
            } else {
                throw new SsfBadRequestException(
                    "Field '" + entry.getKey() + "' in 'complex' format must be a Subject Identifier Object."
                );
            }
        }
    }
}