package com.skillbox.web.contoller;

import com.skillbox.service.EventService;
import com.skillbox.web.dto.event.EventFilterRequest;
import com.skillbox.web.dto.event.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Контроллер для взаимодействия анонимных и зарегистрированных пользователей",
        description = "Энд-поинты данного контроллера позволяют взаимодействовать с мероприятиями в системе " +
                "незарегистрированным пользователям, так и зарегистрированным"
)
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @Operation(
            summary = "Получение всех мероприятий",
            description = "С помощью данного энд-поинта можно получить все мероприятия, которые есть в" +
                    " системе. Так же мероприятия отдаются постранично, с помощью объекта pageable"
    )
    @ApiResponse(responseCode = "200", description = "Мероприятия успешно найдены")
    @GetMapping
    public ResponseEntity<Page<EventResponse>> getAll(
            @RequestBody(
                    description = "Фильтр мероприятий, который фильтрует мероприятия по их " +
                            "дате проведения",
                    required = false
            )
            @ModelAttribute EventFilterRequest filter,
                                  @RequestBody(
                                          description = "Объект pageable с помощью его параметров " +
                                                  "данные выдаются постранично, а не все сразу",
                                          required = true
                                  )
                                  Pageable pageable){
        return ResponseEntity.ok(
                eventService.findAll(filter,pageable)
        );
    }

    @Operation(
            summary = "Получение мероприятия по его id",
            description = "С помощью данного энд-поинта на фронте отображаются данные об мероприятии" +
                    ", далее в этом окне пользователь может совершить бронирование на мероприятие"
    )
    @ApiResponse(responseCode = "200", description = "Мероприятие успешно найдено")
    @ApiResponse(responseCode = "404", description = "Мероприятие по его id не найдено")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(
                eventService.findById(id)
        );
    }
}
