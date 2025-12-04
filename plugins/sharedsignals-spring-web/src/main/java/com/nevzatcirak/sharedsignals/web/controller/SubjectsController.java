package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.facade.AuthFacade;
import com.nevzatcirak.sharedsignals.api.service.SubjectManagementService;
import com.nevzatcirak.sharedsignals.web.mapper.SubjectMapper;
import com.nevzatcirak.sharedsignals.web.model.SSFAddSubjectRequest;
import com.nevzatcirak.sharedsignals.web.model.SSFRemoveSubjectRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Subject Management.
 * <p>
 * Implements SSF Draft Spec Section 8.1.3 (Subjects).
 * <p>
 * Endpoints:
 * - POST /ssf/subjects/add - Add a subject to a stream
 * - POST /ssf/subjects/remove - Remove a subject from a stream
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-secevent-sharedsignals">SSF Specification</a>
 */
@RestController
@RequestMapping("/ssf/subject")
public class SubjectsController {

    private final SubjectManagementService subjectService;
    private final AuthFacade authFacade;
    private final SubjectMapper subjectMapper;

    public SubjectsController(
            SubjectManagementService subjectService,
            AuthFacade authFacade,
            SubjectMapper subjectMapper) {
        this.subjectService = subjectService;
        this.authFacade = authFacade;
        this.subjectMapper = subjectMapper;
    }

    /**
     * Adds a subject to an Event Stream.
     * <p>
     * SSF Spec Section 8.1.3.2: Adding a Subject to a Stream
     * <p>
     * The Transmitter MAY silently ignore the request (e.g., if the subject has opted out).
     * In such cases, returns 200 OK without actually adding the subject.
     *
     * @param request the add subject request containing stream_id, subject, and optional verified flag
     * @return 200 OK on success (empty body per spec)
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addSubject(@RequestBody SSFAddSubjectRequest request) {
        // Fail-fast validation
        if (request.getStreamId() == null || request.getStreamId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getSubject() == null || request.getSubject().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String clientId = authFacade.getClientId();
        subjectService.addSubject(subjectMapper.toCommand(request), clientId);

        // SSF Spec 8.1.3.2: Return empty 200 OK
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .build();
    }

    /**
     * Removes a subject from an Event Stream.
     * <p>
     * SSF Spec Section 8.1.3.3: Removing a Subject
     * <p>
     * The Transmitter MAY silently ignore the request if the subject is not recognized.
     * In such cases, returns 204 No Content without error.
     *
     * @param request the remove subject request containing stream_id and subject
     * @return 204 No Content on success (per spec)
     */
    @PostMapping("/remove")
    public ResponseEntity<Void> removeSubject(@RequestBody SSFRemoveSubjectRequest request) {
        // Fail-fast validation
        if (request.getStreamId() == null || request.getStreamId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getSubject() == null || request.getSubject().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String clientId = authFacade.getClientId();
        subjectService.removeSubject(subjectMapper.toCommand(request), clientId);

        // SSF Spec 8.1.3.3: Return 204 No Content
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }
}