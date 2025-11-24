package com.skillbox.web.contoller;

import com.skillbox.service.AuthService;
import com.skillbox.web.dto.user.AuthUserResponse;
import com.skillbox.web.dto.user.CredentialUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthUserResponse> register(@RequestBody CredentialUserRequest request){

    }
}
