package com.skillbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class NotificationSettings {
    @Column(name = "notify_new_events", nullable = false)
    private Boolean notifyNewEvents = false;

    @Column(name = "notify_upcoming", nullable = false)
    private Boolean notifyUpcoming = true;

    @Column(name = "notify_before_hours", nullable = false)
    private Integer notifyBeforeHours = 2;
}
