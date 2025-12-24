package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.enums.SubjectStatus;
import com.nevzatcirak.sharedsignals.api.facade.AuthFacade;
import com.nevzatcirak.sharedsignals.api.service.StreamAdministrationService;
import com.nevzatcirak.sharedsignals.web.model.StreamModeRequest;
import com.nevzatcirak.sharedsignals.web.model.UpdateAuthorizedEventsRequest;
import com.nevzatcirak.sharedsignals.web.model.UpdateSubjectStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Operations", description = "Management endpoints for SSF Security & Privacy")
@SecurityRequirement(name = "bearer-key")
public class AdminController {

    private final StreamAdministrationService adminService;
    private final AuthFacade authFacade;

    public AdminController(StreamAdministrationService adminService, AuthFacade authFacade) {
        this.adminService = adminService;
        this.authFacade = authFacade;
    }

    @PutMapping("/stream/{streamId}/subject/status")
    @Operation(summary = "Update Subject Status", description = "Approve/Reject subject by its Hash.")
    public ResponseEntity<Void> updateSubjectStatus(
            @PathVariable("streamId") String streamId,
            @RequestBody UpdateSubjectStatusRequest request) {

        adminService.updateSubjectStatus(
            streamId,
            request.getSubjectHash(),
            SubjectStatus.fromValue(request.getStatus()),
            authFacade.getClientId()
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/stream/{streamId}/events/authorized")
    @Operation(summary = "Set Authorized Events")
    public ResponseEntity<Void> setAuthorizedEvents(
            @PathVariable("streamId")String streamId,
            @RequestBody UpdateAuthorizedEventsRequest request) {

        adminService.updateAuthorizedEvents(
            streamId,
            request.getAuthorizedEvents(),
            authFacade.getClientId()
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/stream/{streamId}/mode")
    @Operation(summary = "Set Stream Broadcast Mode", description = "Enable/Disable 'Process All Subjects' (Firehose) mode.")
    public ResponseEntity<Void> setStreamBroadcastMode(
            @PathVariable("streamId")String streamId,
            @RequestBody StreamModeRequest request) {

        adminService.setStreamBroadcastMode(streamId, request.isProcessAllSubjects(), authFacade.getClientId());
        return ResponseEntity.ok().build();
    }
}