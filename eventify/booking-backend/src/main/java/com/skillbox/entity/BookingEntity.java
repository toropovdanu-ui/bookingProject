package com.skillbox.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking")
public class BookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @Column(name = "ticket_count")
    private int ticketCount;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "expiry_time")
    private Instant expiryTime;

    @Column(name = "reminder_at")
    private Instant reminderAt;

    @Column(name = "reminder_sent")
    private boolean reminderSent;

    private boolean confirmed;

    public BookingEntity(UserEntity user, EventEntity event,
                         int ticketCount, Instant createdAt,
                         Instant expiryTime, Instant reminderAt,
                         boolean reminderSent, boolean confirmed) {
        this.user = user;
        this.event = event;
        this.ticketCount = ticketCount;
        this.createdAt = createdAt;
        this.expiryTime = expiryTime;
        this.reminderAt = reminderAt;
        this.reminderSent = reminderSent;
        this.confirmed = confirmed;
    }
}
