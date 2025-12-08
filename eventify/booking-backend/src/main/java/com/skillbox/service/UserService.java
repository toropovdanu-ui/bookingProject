package com.skillbox.service;

import com.skillbox.entity.NotificationSettings;
import com.skillbox.entity.UserEntity;
import com.skillbox.mapper.NotificationMapper;
import com.skillbox.repository.UserRepository;
import com.skillbox.web.dto.user.NotificationSettingsResponse;
import com.skillbox.web.dto.user.UpdateNotificationSettingsRequest;
import com.skillbox.web.exception.InvalidRequestException;
import com.skillbox.web.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public NotificationSettingsResponse getUserNotifications(Long userId){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ошибка! Зайдите в аккаунт еще раз!"));

        return notificationMapper.toDto(userEntity.getNotificationSettings());
    }

    @Transactional
    public NotificationSettingsResponse updateUserNotifications(Long userId,
                                                                UpdateNotificationSettingsRequest request){
        if(request.getNotifyUpcoming() && request.getNotifyBeforeHours() == null){
            throw new InvalidRequestException("При включённом уведомлении о предстоящих событиях необходимо указать количество часов");
        }

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ошибка! Зайдите в аккаунт еще раз!"));

        checkOnNotifyUpcoming(request,userEntity);

        NotificationSettings notificationSettings = notificationMapper.toEntity(request);
        userEntity.setNotificationSettings(notificationSettings);
        userRepository.save(userEntity);

        return notificationMapper.toDto(notificationSettings);
    }

    @Transactional
    public void deleteNotifications(Long userId){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ошибка! Зайдите в аккаунт еще раз!"));

        userEntity.setNotificationSettings(new NotificationSettings());

        userEntity.getBookings().forEach(booking->{
            if(!booking.isReminderSent()){
                Instant startEvent = booking.getEvent().getDateTime();
                Instant reminderAt = startEvent.minus(
                        Duration.ofHours(
                                userEntity.getNotificationSettings().getNotifyBeforeHours()
                        )
                );

                booking.setReminderAt(reminderAt);
            }
        });
    }

    private void checkOnNotifyUpcoming(UpdateNotificationSettingsRequest request,UserEntity userEntity) {
        userEntity.getBookings().forEach(bookEntity->{

            if(!request.getNotifyUpcoming()){
                bookEntity.setReminderSent(true);
                bookEntity.setReminderAt(null);
                return;
            }

            Instant dateTime = bookEntity.getEvent().getDateTime();
            Instant delta = dateTime.minus(request.getNotifyBeforeHours().longValue(), ChronoUnit.HOURS);

            if(delta.isAfter(Instant.now()) && request.getNotifyUpcoming()){
                bookEntity.setReminderSent(false);
                bookEntity.setReminderAt(delta);
            }
        });
    }
}
