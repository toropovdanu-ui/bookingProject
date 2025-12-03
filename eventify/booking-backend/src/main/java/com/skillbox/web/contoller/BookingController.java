package com.skillbox.web.contoller;

import com.skillbox.security.AppUserDetails;
import com.skillbox.service.BookingService;
import com.skillbox.web.dto.booking.BookingResponse;
import com.skillbox.web.dto.booking.CreateBookingRequest;
import com.skillbox.web.dto.booking.UpdateBookingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Контроллер для взаимодействия с бронированиями для пользователя",
        description = "В данном контроллере содержаться энд-поинты, в которых заложены функции " +
                "для обычных пользователей, а также и для администраторов"
)
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @Operation(
            summary = "Получения бронирования по ее id",
            description = "Данный энд-поинт возвращает бронирование по его id"
    )
    @ApiResponse(responseCode = "200", description = "Бронирование успешно найдено")
    @ApiResponse(responseCode = "404", description = "Бронирование по заданному id не найдено")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @Operation(
            summary = "Обновление количества билетов в бронировании",
            description = "С помощью данного энд-поинта пользователь может " +
                    "обновить билеты, которые он забронировал на мероприятие"
    )
    @ApiResponse(responseCode = "200", description = "Билеты на мероприятие успешно обновлены")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено")
    @ApiResponse(responseCode = "409", description = "Билеты не доступны(раскупили другие)")
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateById(@PathVariable Long id,
                                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                              description = "Содержит новое количество билетов для бронирования",
                                                              required = true
                                                      )
                                                      @Valid @RequestBody UpdateBookingRequest request){
        return ResponseEntity.ok(bookingService.updateById(id,request));
    } 

    @Operation(
            summary = "Отмена бронирования по его id",
            description = "Как админ, так и пользователь может отменять бронирование по его id"
    )
    @ApiResponse(responseCode = "204", description = "Бронирование успешно отменено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelById(@PathVariable Long id){
        bookingService.deleteBooking(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Получение бронирований пользователя",
            description = "Пользователь делает запрос, по jwt токену система понимает " +
                    "кто сделал запрос и возвращает все бронирования пользователя"
    )
    @ApiResponse(responseCode = "200",description = "Бронирования успешно найдены")
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getUserBookings(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(bookingService.getUserBookings(userDetails.getUserId()));
    }

    @Operation(
            summary = "Создание бронирования",
            description = "Пользователь находясь в подробной информации об мероприятии," +
                    " может выбрать количество билетов, которые он готов забронировать " +
                    "и создаться бронирование"
    )
    @ApiResponse(responseCode = "201", description = "Бронирование успешно создано")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Содержит id мероприятия и количество билетов для бронирования",
                    required = true
            )
            @Valid @RequestBody CreateBookingRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(userDetails.getUserId(), request));
    }
}
