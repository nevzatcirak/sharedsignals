package com.nevzatcirak.sharedsignals.persistence.entity;
import jakarta.persistence.*;

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

    @Column(name = "verified", nullable = false)
    private boolean verified = true;

    public void setStream(StreamEntity stream) { this.stream = stream; }
    public void setSubjectPayload(String subjectPayload) { this.subjectPayload = subjectPayload; }
    public void setSubjectHash(String subjectHash) { this.subjectHash = subjectHash; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
