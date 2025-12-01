package com.skillbox.web.contoller;

import com.skillbox.service.BookingService;
import com.skillbox.service.EventService;
import com.skillbox.web.dto.booking.BookingFilterRequest;
import com.skillbox.web.dto.booking.BookingResponse;
import com.skillbox.web.dto.event.EventResponse;
import com.skillbox.web.dto.event.UpsertEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Контроллер предназначенный для админ панели",
        description = """
                Данный контроллер реализует функции админ панели,
                к нему имеет доступ только тот пользователь, который обладает
                ролью 'ADMIN'. Админ может создавать мероприятия, редактировать,
                удалять, также подтверждать и отменять брони. 
                """
)
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final EventService eventService;
    private final BookingService bookingService;

    @Operation(
            summary = "Обновление мероприятия",
            description = "Администратор может обновить поля мероприятие по его id " +
                    ", которые есть в базе данных. В запросе ожидается класс который содержит " +
                    "поля мероприятия"
    )
    @ApiResponse(responseCode = "200", description = "Мероприятие успешно обновлено")
    @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
    @ApiResponse(responseCode = "400", description = "Валидация данных не пройдена")
    @PutMapping("/events/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id,
                                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                             description = "Данные для обновления объекта мероприятия" +
                                                                     " по ее id",
                                                             required = true
                                                     )
                                                     @Valid @RequestBody UpsertEventRequest request){
        return ResponseEntity.ok(eventService.updateEvent(id,request));
    }

    @Operation(
            summary = "Удаление мероприятия",
            description = "Администратор удаляет мероприятие по его id"
    )
    @ApiResponse(responseCode = "204", description = "Мероприятие успешно удалено")
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id){
        eventService.deleteEvent(id);

        return ResponseEntity.noContent()
                .build();
    }

    @Operation(
            summary = "Подтверждение бронирования администратором",
            description = "По id бронирования, администратор подтверждает бронирование" +
                    "и отправляется уведомление пользователю на smtp сервер о том, что бронирование" +
                    " подтверждено"
    )
    @ApiResponse(responseCode = "204", description = "Бронирование успешно подтверждено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено")
    @PutMapping("/bookings/{id}/confirm")
    public ResponseEntity<Void> confirmBooking(@PathVariable Long id){
        bookingService.confirmBooking(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Создание мероприятия администратором",
            description = "Администратор заполняет форму создания мероприятия," +
                    "включая: название, описание, дата проведения, общее количество билетов," +
                    " ссылку на изображение из интернета. В запросе передается класс с формой")
    @ApiResponse(responseCode = "201", description = "Мероприятие успешно создано")
    @ApiResponse(responseCode = "400", description = "Валидация данных не пройдена")
    @ApiResponse(responseCode = "422", description = "Дата проведения мероприятия не может быть раньше," +
            " чем время в данный момент.")
    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные из формы",
                    required = true
            )
            @Valid @RequestBody UpsertEventRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(request));
    }

    @Operation(
            summary = "Получение бронирований исходя из фильтра",
            description = "Администратор может получить множества бронирований," +
                    " поставив свой фильтр, а также требуется Pageable объект, " +
                    "с помощью которого можно вернуть бронирования на конкретной странице"
    )
    @ApiResponse(responseCode = "200", description = "Мероприятия успешно найдены")
    @ApiResponse(responseCode = "400", description = "Формат даты не соответствует заявленному")
    @GetMapping("/bookings")
    public ResponseEntity<Page<BookingResponse>> getBookings(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Класс, который содержит фильтры для бронирования, которые задал админ",
                    required = false
            )
            @ModelAttribute BookingFilterRequest filter,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Класc, который содержит параметры пагинации " +
                            "для выдачи бронирований админу",
                    required = true
            )
            Pageable pageable){
        return ResponseEntity.ok(
                bookingService.getAll(filter,pageable)
        );
    }

    @Operation(
            summary = "Удаление бронирования администратором",
            description = "Администратор удаляет бронирование исходя из его id в БД"
    )
    @ApiResponse(responseCode = "204", description = "Бронирование успешно удалено")
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id){
        bookingService.deleteBooking(id);

        return ResponseEntity.noContent().build();
    }
}
