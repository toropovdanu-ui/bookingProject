package com.skillbox.web.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    @NotNull(message = "Должен быть выбран идентификатор мероприятия")
    private Long eventId;

    @NotNull(message = "Количество билетов не должно быть пустым")
    private int ticketCount;
}
