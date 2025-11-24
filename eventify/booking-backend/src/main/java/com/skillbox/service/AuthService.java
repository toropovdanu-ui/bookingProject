package com.skillbox.service;

import com.skillbox.repository.UserRepository;
import com.skillbox.web.dto.user.AuthUserResponse;
import com.skillbox.web.dto.user.CredentialUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public AuthUserResponse register(CredentialUserRequest request){

    }
}
