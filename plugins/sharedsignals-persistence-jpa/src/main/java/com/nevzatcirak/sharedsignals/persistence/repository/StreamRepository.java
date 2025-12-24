package com.nevzatcirak.sharedsignals.persistence.repository;
import com.nevzatcirak.sharedsignals.persistence.entity.StreamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StreamRepository extends JpaRepository<StreamEntity, String> {
    List<StreamEntity> findByAudience(String audience);

    // Return streams that match the subject hash (and are APPROVED)
    // OR streams that are configured to process ALL subjects (Wildcard Mode).
    // Uses DISTINCT because a stream could potentially match both conditions (though unlikely in logic).
    @Query("SELECT DISTINCT s FROM StreamEntity s " +
           "LEFT JOIN SubjectEntity sub ON s.streamId = sub.stream.streamId AND sub.subjectHash = :subjectHash " +
           "WHERE s.status = 'enabled' " +
           "AND (" +
           "   (s.processAllSubjects = true) " +
           "   OR " +
           "   (sub.subjectHash IS NOT NULL AND sub.status = 'APPROVED')" +
           ")")
    List<StreamEntity> findEnabledStreamsBySubjectHash(@Param("subjectHash") String subjectHash);
}
