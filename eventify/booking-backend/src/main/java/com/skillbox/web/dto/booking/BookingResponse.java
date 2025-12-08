package com.skillbox.web.dto.booking;

import com.skillbox.web.dto.event.EventResponse;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private EventResponse event;
    private String customerEmail;
    private int ticketCount;
    private String createdAt;
    private String expiryTime;
    private boolean confirmed;
}
