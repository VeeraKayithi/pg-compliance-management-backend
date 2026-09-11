package com.pgcompliance.repository;

import com.pgcompliance.entity.AnnouncementDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementDeliveryRepository
        extends JpaRepository<AnnouncementDelivery, Long> {
    List<AnnouncementDelivery> findByAnnouncementAnnouncementIdOrderByCreatedAtAsc(
            Long announcementId
    );
}
