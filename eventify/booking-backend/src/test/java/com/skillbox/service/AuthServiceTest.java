package com.skillbox.service;

import com.skillbox.entity.UserEntity;
import com.skillbox.repository.UserRepository;
import com.skillbox.security.AppUserDetails;
import com.skillbox.security.jwt.JwtUtils;
import com.skillbox.web.dto.user.AuthUserResponse;
import com.skillbox.web.dto.user.UserCredentialRequest;
import com.skillbox.web.exception.UserExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static com.skillbox.entity.RoleType.ROLE_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    private String email = "toropov.danu@gmail.com";

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldReturnUserRole_whenRegisteringWithUniqueEmail(){
        //given
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(new UserEntity());

        //when
        authService.register(new UserCredentialRequest(email, "11111111"));

        //return
        verify(userRepository).save(captor.capture());

        UserEntity captureUser = captor.getValue();

        assertThat(captureUser.getEmail()).isEqualTo(email);
        assertThat(captureUser.getRoles()).isEqualTo(Set.of(ROLE_USER));

        verify(encoder,times(1)).encode("11111111");
    }

    @Test
    void registerUser_whenEmailIsAvailable_returnTokenAndRole(){
        // given
        UserEntity savedUser = new UserEntity();
        String token = "12345678";
        AuthUserResponse authUserResponse = AuthUserResponse.builder()
                .token(token)
                .role("USER")
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(savedUser);
        when(jwtUtils.generateTokenFromUsername(any())).thenReturn(token);

        // when
        AuthUserResponse registerUser =
                authService.register(new UserCredentialRequest(email, "11111111"));

        //return
        assertThat(registerUser).isEqualTo(authUserResponse);
    }

    @Test
    void registerUser_whenEmailIsNotUnique_returnUserExistsException(){
        //given
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new UserEntity()));

        //return
        assertThatThrownBy(()->authService.register(new UserCredentialRequest()))
                .isInstanceOf(UserExistsException.class)
                .hasMessage("Ошибка! Пользователь с таким email уже есть в системе");
    }

    @Test
    void signIn_whenDataFromRequestMatchesInDB_returnTokenAndRole(){
        //given
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setRoles(Set.of(ROLE_USER));

        AppUserDetails userDetails = new AppUserDetails(userEntity);

        Authentication authenticate = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authenticate);
        when(jwtUtils.generateTokenFromUsername(any())).thenReturn("12345678");

        //when
        AuthUserResponse authUserResponse = authService.signIn(new UserCredentialRequest());

        //return
        assertThat(authUserResponse).isEqualTo(
                AuthUserResponse.builder()
                        .token("12345678")
                        .role("USER")
                        .build()
        );
    }
}
