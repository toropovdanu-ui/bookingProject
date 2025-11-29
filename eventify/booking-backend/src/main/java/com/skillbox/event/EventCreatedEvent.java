package com.skillbox.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventCreatedEvent{
    private String eventName;

    public EventCreatedEvent(String nameEvent) {
        this.eventName = nameEvent;
    }
}
