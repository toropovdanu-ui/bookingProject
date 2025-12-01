package com.skillbox.web.contoller;

import com.skillbox.service.AuthService;
import com.skillbox.web.dto.user.AuthUserResponse;
import com.skillbox.web.dto.user.UserCredentialRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Контроллер предназначенный для аутентификации и регистрации",
        description = "Данный класс содержит энд-поинты регистрации и аутентификации. " +
                "При регистрации пользователь попадает сразу в личный кабинет, а на фронт передается" +
                " jwt-token, с помощью которого потом можно на бекенде опознать пользователя." +
                " При аутентификации также выдается токен. Аутентификация и регистрация реализована " +
                "с помощью jwt-токена stateless состояния. Срок истечения токена 30 дней"

)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Регистрация пользователя на сайте",
            description = """
                    С помощью данного энд-поинта пользователь может зарегистрироваться на сайте,
                    после регистрации выдается jwt токен, он должен храниться в локальном хранилище фронта.
                    """,
            security = {}
    )
    @ApiResponse(responseCode = "201",description = "Пользователь успешно зарегистрирован")
    @ApiResponse(responseCode = "400", description = "Валидация данных не пройдена")
    @PostMapping("/register")
    public ResponseEntity<AuthUserResponse> register(@Valid @RequestBody UserCredentialRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Аутентификация пользователя",
            description = """
                    Система может аутентифицировать пользователя по его введенным данным,
                    логин и пароль. Как и при регистрации, бекенд в ответе отдает jwt-токен.
                    """,
            security = {}
    )
    @ApiResponse(responseCode = "200", description = "Пользователь успешно аутентифицирован")
    @ApiResponse(responseCode = "400", description = "Данные не прошли валидацию")
    @ApiResponse(responseCode = "404", description = "Пользователь по email не найден")
    public ResponseEntity<AuthUserResponse> signIn(@Valid @RequestBody UserCredentialRequest request){
        return ResponseEntity.ok(authService.signIn(request));
    }
}
