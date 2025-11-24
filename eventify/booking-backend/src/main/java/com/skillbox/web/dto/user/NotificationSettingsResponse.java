package com.skillbox.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsResponse {
    private Boolean notifyNewEvents;
    private Boolean notifyUpcoming;
    private Integer notifyBeforeHours;
}
