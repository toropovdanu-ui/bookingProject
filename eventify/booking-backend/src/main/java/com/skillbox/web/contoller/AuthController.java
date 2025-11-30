package com.skillbox.web.contoller;

import com.skillbox.service.AuthService;
import com.skillbox.web.dto.user.AuthUserResponse;
import com.skillbox.web.dto.user.UserCredentialRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @Operation(security = {})
    public ResponseEntity<AuthUserResponse> register(@RequestBody UserCredentialRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(security = {})
    public ResponseEntity<AuthUserResponse> signIn(@RequestBody UserCredentialRequest request){
        return ResponseEntity.ok(authService.signIn(request));
    }
}
