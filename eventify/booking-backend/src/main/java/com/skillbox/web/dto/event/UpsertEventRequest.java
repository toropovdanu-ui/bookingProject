package com.skillbox.web.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpsertEventRequest {
    private String title;
    private String description;
    private Instant dateTime;
    private int totalTickets;
    private String coverUrl;
}
