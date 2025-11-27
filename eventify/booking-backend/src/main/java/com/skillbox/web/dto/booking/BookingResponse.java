package com.skillbox.web.dto.booking;

import com.skillbox.web.dto.event.EventResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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
