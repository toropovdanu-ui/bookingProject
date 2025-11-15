package com.skillbox.web.dto.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventFilterRequest {
    private String from;
    private String to;
}
