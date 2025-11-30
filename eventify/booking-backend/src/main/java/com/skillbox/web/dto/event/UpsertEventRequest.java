package com.skillbox.web.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpsertEventRequest {
    @NotBlank(message = "Название мероприятия не должно быть пустым")
    private String title;

    @NotBlank(message = "Описание мероприятия должно быть заполненным")
    private String description;

    @NotBlank(message = "Дата проведения мероприятия обязательна")
    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?(Z|[+-]\\d{2}:\\d{2})?$",
            message = "Неверный формат даты. Ожидается ISO 8601: yyyy-MM-ddTHH:mm:ss[.nnnnnnnnn][Z или ±HH:mm]"
    )
    private String dateTime;

    @NotNull(message = "Количество билетов на мероприятие не должно быть пустым")
    private int totalTickets;

    private String coverUrl;
}
