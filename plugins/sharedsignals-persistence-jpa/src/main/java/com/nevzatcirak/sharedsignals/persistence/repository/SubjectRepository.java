package com.nevzatcirak.sharedsignals.persistence.repository;
import com.nevzatcirak.sharedsignals.persistence.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {
    boolean existsByStreamStreamIdAndSubjectHash(String streamId, String subjectHash);
    Optional<SubjectEntity> findByStreamStreamIdAndSubjectHash(String streamId, String subjectHash);
}
