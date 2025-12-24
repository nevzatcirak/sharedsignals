package com.nevzatcirak.sharedsignals.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update the approval status of a subject")
public class UpdateSubjectStatusRequest {

    @Schema(description = "The unique hash of the subject", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("subject_hash")
    private String subjectHash;

    @Schema(description = "New status (approved, rejected, pending)", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("status")
    private String status;

    public String getSubjectHash() { return subjectHash; }
    public void setSubjectHash(String subjectHash) { this.subjectHash = subjectHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
