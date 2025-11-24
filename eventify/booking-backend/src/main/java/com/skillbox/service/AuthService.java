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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthUserResponse register(UserCredentialRequest request){
        UserEntity userEntity = new UserEntity();
        userEntity.initialize(request,encoder);

        UserEntity savedUser = userRepository.save(userEntity);
        String jwtToken = jwtUtils.generateJwtToken(new AppUserDetails(savedUser));
        String role = savedUser.getRoles().get(0).name();
        String substringRole = role.substring(5);

        AppUserDetails userDetails = new AppUserDetails(savedUser);

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authenticate);

        return AuthUserResponse.builder()
                .token(jwtToken)
                .role(substringRole)
                .build();
    }

    public AuthUserResponse signIn(UserCredentialRequest request){
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authenticate);

        AppUserDetails userDetails = (AppUserDetails) authenticate.getPrincipal();

        String jwtToken = jwtUtils.generateJwtToken(userDetails);
        RoleType roleType = userDetails.getAuthorities().size() == 1 ? RoleType.ROLE_USER : RoleType.ROLE_ADMIN;
        String role = roleType.name().substring(5);

        return AuthUserResponse.builder()
                .token(jwtToken)
                .role(role)
                .build();
    }
}
