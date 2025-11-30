package com.skillbox.web.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialRequest {
    @NotBlank
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank
    @Size(min = 8, max = 100, message = "Пароль должен быть от 8 до 100 символов")
    private String password;
}
