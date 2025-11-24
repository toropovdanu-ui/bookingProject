package com.skillbox.service;

import com.skillbox.entity.UserEntity;
import com.skillbox.repository.UserRepository;
import com.skillbox.web.dto.user.NotificationSettingsResponse;
import com.skillbox.web.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public NotificationSettingsResponse userNotifications(Long userId){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ошибка! Зайдите в аккаунт еще раз!"));

        return
    }
}
