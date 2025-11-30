package com.skillbox.web.exception;

public class EventStartInPastException extends IllegalArgumentException {
    public EventStartInPastException(String message) {
        super(message);
    }
}
