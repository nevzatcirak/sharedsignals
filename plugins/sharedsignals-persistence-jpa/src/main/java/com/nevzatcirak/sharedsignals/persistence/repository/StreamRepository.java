package com.nevzatcirak.sharedsignals.persistence.repository;
import com.nevzatcirak.sharedsignals.persistence.entity.StreamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StreamRepository extends JpaRepository<StreamEntity, String> {
    List<StreamEntity> findByAudience(String audience);

    // Find enabled streams that have a matching subject entry
    @Query("SELECT s FROM StreamEntity s JOIN SubjectEntity sub ON s.streamId = sub.stream.streamId " +
           "WHERE s.status = 'enabled' AND sub.subjectHash = :subjectHash")
    List<StreamEntity> findEnabledStreamsBySubjectHash(String subjectHash);
}
