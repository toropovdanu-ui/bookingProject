package com.skillbox.repository;

import com.skillbox.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<EventEntity,Long>, JpaSpecificationExecutor<EventEntity> {

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE EventEntity e
    SET e.availableTickets = e.availableTickets + :delta
    WHERE e.id = :eventId
    """)
    void updateAvailableTickets(@Param("eventId") Long eventId,
                                @Param("delta") int delta);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE EventEntity e
            SET e.availableTickets = e.availableTickets - :ticket
            WHERE e.id = :eventId
            """)
    void reduceAvailableTickets(@Param("eventId") Long eventId,
                                @Param("ticket") int ticket);
}
