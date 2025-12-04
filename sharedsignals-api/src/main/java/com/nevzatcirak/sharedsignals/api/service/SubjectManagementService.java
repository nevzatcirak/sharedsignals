package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.AddSubjectCommand;
import com.nevzatcirak.sharedsignals.api.model.RemoveSubjectCommand;

/**
 * Service interface for Subject Management operations.
 * <p>
 * SSF Spec Section 8.1.3: Subjects
 */
public interface SubjectManagementService {

    /**
     * Adds a subject to a stream.
     * <p>
     * SSF Spec Section 8.1.3.2: Adding a Subject to a Stream
     *
     * @param command the add subject command
     * @param owner the authenticated client ID (from JWT)
     */
    void addSubject(AddSubjectCommand command, String owner);

    /**
     * Removes a subject from a stream.
     * <p>
     * SSF Spec Section 8.1.3.3: Removing a Subject
     *
     * @param command the remove subject command
     * @param owner the authenticated client ID (from JWT)
     */
    void removeSubject(RemoveSubjectCommand command, String owner);
}