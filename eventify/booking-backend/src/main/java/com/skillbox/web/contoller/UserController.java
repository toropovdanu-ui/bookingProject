package com.skillbox.web.contoller;

import com.skillbox.security.AppUserDetails;
import com.skillbox.service.UserService;
import com.skillbox.web.dto.user.NotificationSettingsResponse;
import com.skillbox.web.dto.user.UpdateNotificationSettingsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Контроллер для взаимодействия пользователя с настройками нотификации",
        description = "С помощью данного контроллера пользователь может поменять настройки " +
                "нотификации и выбрать такие какие ему удобны, по умолчанию нотификация о новых " +
                "мероприятиях выключена, а нотификация по его бронированию включена на 2 часа до начала мероприятия"
)
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Получение настроек нотификации пользователя",
            description = "Когда пользователь переходит во вкладку настройки, " +
                    "данный энд поинт передает настройки заданного пользователя"
    )
    @ApiResponse(responseCode = "200", description = "Настройки успешно найдены")
    @ApiResponse(responseCode = "404", description = "Пользователь сделавший запрос не найден")
    @GetMapping("/notifications")
    public ResponseEntity<NotificationSettingsResponse> getUserNotifications(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(userService.getUserNotifications(userDetails.getUserId()));
    }

    @Operation(
            summary = "Обновление настроек пользователя",
            description = "С помощью данного энд-поинта пользователь может поменять " +
                    "настройки нотификации. Уведомлять о новых мероприятиях или нет, " +
                    "за какое время уведомлять о начале мероприятия по бронированию."
    )
    @ApiResponse(responseCode = "200", description = "Настройки нотификации успешно обновлены")
    @ApiResponse(responseCode = "400", description = "Необходимо указать количество часов за которое нужно уведомлять " +
            "до начала мероприятия в бронировании")
    @ApiResponse(responseCode = "404", description = "Пользователь сделавший запрос не найден")
    @PutMapping("/notifications")
    public ResponseEntity<NotificationSettingsResponse> updateUserNotifications(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Класс с обновленными настройками нотификации",
                    required = true
            )
            @RequestBody UpdateNotificationSettingsRequest request
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(userService.updateUserNotifications(userDetails.getUserId(),request));
    }

    @Operation(
            summary = "Сброс настроек нотификации пользователя",
            description = "С помощью данного энд-поинта пользователь может сбросить настройки нотификации " +
                    "до заводских настроек"
    )
    @ApiResponse(responseCode = "204", description = "Настройки успешно сброшены")
    @ApiResponse(responseCode = "404", description = "Пользователь сделавший запрос не найден")
    @DeleteMapping("/notifications")
    public ResponseEntity<Void> deleteNotification(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        userService.deleteNotifications(userDetails.getUserId());

        return ResponseEntity.noContent().build();
    }
}
