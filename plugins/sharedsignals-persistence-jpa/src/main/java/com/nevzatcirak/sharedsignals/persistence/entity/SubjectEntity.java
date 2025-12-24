package com.nevzatcirak.sharedsignals.persistence.entity;

import com.nevzatcirak.sharedsignals.api.enums.SubjectStatus;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * JPA Entity for Subjects monitored by a stream.
 */
@Entity
@Table(name = "ssf_stream_subjects", indexes = @Index(name = "idx_stream_subject_hash", columnList = "stream_id, subject_hash"))
public class SubjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private StreamEntity stream;

    @Column(name = "subject_payload", nullable = false, columnDefinition = "TEXT")
    private String subjectPayload;

    @Column(name = "subject_hash", nullable = false)
    private String subjectHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubjectStatus status = SubjectStatus.PENDING;

    public SubjectEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StreamEntity getStream() { return stream; }
    public void setStream(StreamEntity stream) { this.stream = stream; }
    public String getSubjectHash() { return subjectHash; }
    public void setSubjectHash(String subjectHash) { this.subjectHash = subjectHash; }
    public String getSubjectPayload() { return subjectPayload; }
    public void setSubjectPayload(String subjectPayload) { this.subjectPayload = subjectPayload; }
    public SubjectStatus getStatus() { return status; }
    public void setStatus(SubjectStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubjectEntity that)) return false;
        return Objects.equals(getSubjectHash(), that.getSubjectHash()) &&
               Objects.equals(getStream().getStreamId(), that.getStream().getStreamId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStream().getStreamId(), getSubjectHash());
    }
}
