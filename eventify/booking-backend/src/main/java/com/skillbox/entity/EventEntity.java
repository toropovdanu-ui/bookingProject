package com.skillbox.entity;

import com.skillbox.utils.DateTimeUtils;
import com.skillbox.web.dto.event.UpsertEventRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "event")
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    private String title;

    private String description;

    @Column(name = "date_time")
    private Instant dateTime;

    @Column(name = "total_tickets")
    private int totalTickets;

    @Column(name = "available_tickets")
    private int availableTickets;

    @Column(name = "cover_url")
    private String coverUrl;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookingEntity> bookings = new HashSet<>();

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public void updateFrom(UpsertEventRequest request, int availableTickets){
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.dateTime = DateTimeUtils.parseDate(request.getDateTime());
        this.totalTickets = request.getTotalTickets();
        this.availableTickets = availableTickets;
        this.coverUrl = request.getCoverUrl();
        this.updatedAt = Instant.now();
    }
}
