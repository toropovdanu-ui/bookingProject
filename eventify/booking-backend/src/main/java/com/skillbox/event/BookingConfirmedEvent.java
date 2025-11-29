package com.skillbox.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookingConfirmedEvent {
    private String emailUser;
    private String eventTitle;

    public BookingConfirmedEvent(String emailUser, String eventTitle) {
        this.emailUser = emailUser;
        this.eventTitle = eventTitle;
    }
}
