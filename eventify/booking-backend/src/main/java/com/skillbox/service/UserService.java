package com.skillbox.service;

import com.skillbox.entity.NotificationSettings;
import com.skillbox.entity.UserEntity;
import com.skillbox.mapper.NotificationMapper;
import com.skillbox.repository.UserRepository;
import com.skillbox.web.dto.user.NotificationSettingsResponse;
import com.skillbox.web.dto.user.UpdateNotificationSettingsRequest;
import com.skillbox.web.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public NotificationSettingsResponse userNotifications(Long userId){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ошибка! Зайдите в аккаунт еще раз!"));

        return notificationMapper.toDto(userEntity.getNotificationSettings());
    }

    @Transactional
    public NotificationSettingsResponse updateUserNotifications(Long userId,
                                                                UpdateNotificationSettingsRequest request){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ошибка! Зайдите в аккаунт еще раз!"));

        NotificationSettings notificationSettings = notificationMapper.toEntity(request);

        userEntity.setNotificationSettings(notificationSettings);

        return notificationMapper.toDto(notificationSettings);
    }

    @Transactional
    public void deleteNotifications(Long userId){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ошибка! Зайдите в аккаунт еще раз!"));

        userEntity.setNotificationSettings(new NotificationSettings());
    }
}
