package com.skillbox.web.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private Instant dateTime;
    private int totalTickets;
    private int availableTickets;
    private String coverUrl;
}
