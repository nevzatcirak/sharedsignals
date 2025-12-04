package com.nevzatcirak.sharedsignals.core.builder;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for constructing standard Subject objects.
 */
public class SubjectBuilder {
    public static Map<String, Object> createIssSub(String issuerUrl, String subjectId) {
        Map<String, Object> subject = new HashMap<>();
        subject.put("format", SharedSignalConstants.FORMAT_ISSUER_SUBJECT);
        subject.put("iss", issuerUrl);
        subject.put("sub", subjectId);
        return subject;
    }
}
