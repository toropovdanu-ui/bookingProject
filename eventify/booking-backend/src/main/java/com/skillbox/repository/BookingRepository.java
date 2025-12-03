package com.skillbox.repository;

import com.skillbox.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity,Long>, JpaSpecificationExecutor<BookingEntity> {

    @Query("""
            SELECT b
            FROM BookingEntity b
            WHERE b.user.id = :userId
            """)
    List<BookingEntity> findAllByUserId(@Param("userId")Long userId);

    @Query(
            value = """
        SELECT * 
        FROM booking b
        WHERE b.reminder_at <= :now 
          AND b.reminder_at >= :nowMinus5Minutes
          AND b.reminder_sent = false
        FOR UPDATE SKIP LOCKED
        """,
            nativeQuery = true
    )
    List<BookingEntity> findBookingsWithReminder(
            @Param("now") Instant now,
            @Param("nowMinus5Minutes") Instant nowMinus5Minutes
    );

    @Modifying
    @Query("""
    UPDATE BookingEntity b
    SET reminderSent = true
    WHERE b.id = :bookingId
    """)
    int updateReminderSentToTrue(@Param("bookingId") Long bookingId);
}
