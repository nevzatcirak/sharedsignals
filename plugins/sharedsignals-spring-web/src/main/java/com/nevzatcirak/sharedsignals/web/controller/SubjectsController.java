package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.facade.AuthFacade;
import com.nevzatcirak.sharedsignals.api.service.SubjectManagementService;
import com.nevzatcirak.sharedsignals.web.mapper.SubjectMapper;
import com.nevzatcirak.sharedsignals.web.model.SSFAddSubjectRequest;
import com.nevzatcirak.sharedsignals.web.model.SSFRemoveSubjectRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ProblemDetail;
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
@Tag(name = "Subject Management", description = "Managing subjects within a stream. SSF Spec Section 8.1.3.")
@SecurityRequirement(name = "bearer-key")
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
    @Operation(
        summary = "Add Subject",
        description = "Adds a subject to the stream. NOTE: Per spec, the transmitter MAY return 200 OK even if the subject is not added (e.g., opted-out) to prevent probing."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subject added (or silently ignored)."),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Stream not found.", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @Operation(summary = "Remove Subject", description = "Removes a subject from the stream.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Subject removed successfully."),
        @ApiResponse(responseCode = "404", description = "Stream not found.", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
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