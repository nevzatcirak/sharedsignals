package com.nevzatcirak.sharedsignals.persistence.repository;

import com.nevzatcirak.sharedsignals.persistence.entity.StreamEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StreamEventRepository extends JpaRepository<StreamEventEntity, Long> {
    List<StreamEventEntity> findByStreamIdOrderByCreatedAtAsc(String streamId, Pageable pageable);
    void deleteByStreamIdAndJtiIn(String streamId, List<String> jtis);
    long countByStreamId(String streamId);
}
