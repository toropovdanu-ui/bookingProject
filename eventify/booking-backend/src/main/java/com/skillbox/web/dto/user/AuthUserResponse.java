package com.skillbox.web.dto.user;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUserResponse {
    private String token;
    private String role;
}
