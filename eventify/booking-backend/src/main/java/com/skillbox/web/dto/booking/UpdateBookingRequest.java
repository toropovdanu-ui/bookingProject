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
public class UpdateBookingRequest {

    @NotNull(message = "Количество билетов не должно быть пустым")
    private int ticketCount;
}
