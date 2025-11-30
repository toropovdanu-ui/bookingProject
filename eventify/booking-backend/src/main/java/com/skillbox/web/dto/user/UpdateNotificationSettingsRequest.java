package com.skillbox.web.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationSettingsRequest {
    @NotNull(message = "Поле notifyNewEvents должно содержать значение!")
    private Boolean notifyNewEvents;

    @NotNull(message = "Поле notifyUpcoming должно содержать значение!")
    private Boolean notifyUpcoming;

    private Integer notifyBeforeHours;
}
