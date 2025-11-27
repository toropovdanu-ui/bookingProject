package com.skillbox.service;

import com.skillbox.entity.RoleType;
import com.skillbox.entity.UserEntity;
import com.skillbox.repository.UserRepository;
import com.skillbox.security.AppUserDetails;
import com.skillbox.security.jwt.JwtUtils;
import com.skillbox.web.dto.user.AuthUserResponse;
import com.skillbox.web.dto.user.UserCredentialRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthUserResponse register(UserCredentialRequest request){
        UserEntity userEntity = new UserEntity();
        userEntity.initialize(request,encoder);

        UserEntity savedUser = userRepository.save(userEntity);
        String jwtToken = jwtUtils.generateTokenFromUsername(request.getEmail());
        String role = savedUser.getRoles().stream()
                .map(RoleType::name)
                .findFirst()
                .orElse("ROLE_USER")
                .replace("ROLE_", "");

        AppUserDetails userDetails = new AppUserDetails(savedUser);

        Authentication authenticate = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authenticate);

        return AuthUserResponse.builder()
                .token(jwtToken)
                .role(role)
                .build();
    }

    @Transactional
    public AuthUserResponse signIn(UserCredentialRequest request){
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authenticate);

        AppUserDetails userDetails = (AppUserDetails) authenticate.getPrincipal();

        String jwtToken = jwtUtils.generateTokenFromUsername(request.getEmail());
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(r -> r.contains("ADMIN"))
                .findFirst()
                .orElse("ROLE_USER")
                .replace("ROLE_", "");


        return AuthUserResponse.builder()
                .token(jwtToken)
                .role(role)
                .build();
    }
}
