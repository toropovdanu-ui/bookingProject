package com.skillbox.service;

import com.skillbox.entity.BookingEntity;
import com.skillbox.entity.EventEntity;
import com.skillbox.entity.NotificationSettings;
import com.skillbox.entity.UserEntity;
import com.skillbox.mapper.NotificationMapper;
import com.skillbox.repository.UserRepository;
import com.skillbox.web.dto.user.NotificationSettingsResponse;
import com.skillbox.web.dto.user.UpdateNotificationSettingsRequest;
import com.skillbox.web.exception.InvalidRequestException;
import com.skillbox.web.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserNotifications_whenUserIsLoggedIn_returnDefaultNotificationSettings(){
        //given
        ArgumentCaptor<NotificationSettings> captor = ArgumentCaptor.forClass(NotificationSettings.class);

        when(userRepository.findById(any())).thenReturn(Optional.of(new UserEntity()));
        when(notificationMapper.toDto(captor.capture())).thenReturn(new NotificationSettingsResponse());

        //when
        userService.getUserNotifications(1L);

        //assert
        NotificationSettings ntSettings = captor.getValue();

        assertThat(ntSettings.getNotifyNewEvents()).isEqualTo(false);
        assertThat(ntSettings.getNotifyUpcoming()).isEqualTo(true);
        assertThat(ntSettings.getNotifyBeforeHours()).isEqualTo(2);

        verify(userRepository,times(1)).findById(1L);
        verify(notificationMapper,times(1)).toDto(any());
    }

    @Test
    void getUserNotifications_whenUserNotFound_returnUserNotFoundException(){
        //given
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //assert
        assertThatThrownBy(()->userService.getUserNotifications(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Ошибка! Зайдите в аккаунт еще раз!");
    }

    @Test
    void updateUserNotifications_whenDataFromRequestIsValid_returnUpdatedUserNotificationsSettings(){
        //given
        UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest();
        request.setNotifyNewEvents(false);
        request.setNotifyUpcoming(true);
        request.setNotifyBeforeHours(2);

        when(userRepository.findById(any())).thenReturn(Optional.of(new UserEntity()));
        when(notificationMapper.toEntity(request)).thenReturn(new NotificationSettings());
        when(userRepository.save(any())).thenReturn(new UserEntity());
        when(notificationMapper.toDto(any())).thenReturn(new NotificationSettingsResponse());

        //when
        userService.updateUserNotifications(1L,request);

        //assert
        verify(userRepository,times(1)).findById(any());
        verify(notificationMapper,times(1)).toEntity(request);
        verify(userRepository,times(1)).save(any());
    }

    @Test
    void checkOnNotifyUpcoming_whenNotifyUpcomingIsFalse_shouldBookingReminderSentIsTrue(){
        //given
        UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest();
        request.setNotifyNewEvents(false);
        request.setNotifyUpcoming(false);
        request.setNotifyBeforeHours(null);

        NotificationSettings entityFromRequest = new NotificationSettings();
        entityFromRequest.setNotifyNewEvents(request.getNotifyNewEvents());
        entityFromRequest.setNotifyUpcoming(request.getNotifyUpcoming());
        entityFromRequest.setNotifyBeforeHours(request.getNotifyBeforeHours());

        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        BookingEntity booking1 = new BookingEntity();
        booking.setId(2L);

        UserEntity user = new UserEntity();
        user.setBookings(Set.of(booking,booking1));

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(notificationMapper.toEntity(request)).thenReturn(entityFromRequest);
        when(userRepository.save(any())).thenReturn(new UserEntity());
        when(notificationMapper.toDto(any())).thenReturn(new NotificationSettingsResponse());

        //when
        userService.updateUserNotifications(1L,request);

        //assert
        assertThat(booking.isReminderSent()).isTrue();
        assertThat(booking1.isReminderSent()).isTrue();
    }

    @Test
    void checkOnNotifyUpcoming_whenNotifyUpcomingIsTrue_shouldReminderSentIsFalse(){
        //given
        UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest();
        request.setNotifyNewEvents(false);
        request.setNotifyUpcoming(true);
        request.setNotifyBeforeHours(2);

        EventEntity event = new EventEntity();
        event.setDateTime(Instant.parse("2199-12-07T10:30:00Z"));

        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setEvent(event);
        BookingEntity booking1 = new BookingEntity();
        booking.setId(2L);
        booking1.setEvent(event);

        UserEntity user = new UserEntity();
        user.setBookings(Set.of(booking,booking1));

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(notificationMapper.toEntity(any())).thenReturn(new NotificationSettings());
        when(userRepository.save(any())).thenReturn(new UserEntity());
        when(notificationMapper.toDto(any())).thenReturn(new NotificationSettingsResponse());

        //when
        userService.updateUserNotifications(1L,request);

        //assert
        assertThat(booking.isReminderSent()).isFalse();
        assertThat(booking1.isReminderSent()).isFalse();
    }

    @Test
    void updateUserNotifications_whenNotifyUpcIsTrueAndBeforeHoursIsNull_returnInvalidRequestException(){
        //given
        UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest();
        request.setNotifyNewEvents(false);
        request.setNotifyUpcoming(true);
        request.setNotifyBeforeHours(null);

        //assert
        assertThatThrownBy(()->userService.updateUserNotifications(1L,request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("При включённом уведомлении о предстоящих событиях необходимо указать количество часов");
    }

    @Test
    void updateUserNotifications_whenUserNotFound_returnUserNotFoundException(){
        //given
        UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest();
        request.setNotifyNewEvents(false);
        request.setNotifyUpcoming(true);
        request.setNotifyBeforeHours(2);

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //assert
        assertThatThrownBy(()->userService.updateUserNotifications(1L,request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Ошибка! Зайдите в аккаунт еще раз!");
    }

    @Test
    void deleteNotifications_whenUserIsExists_shouldNotificationsIsDefault(){
        //given
        NotificationSettings notificationSettings = new NotificationSettings();
        notificationSettings.setNotifyNewEvents(true);
        notificationSettings.setNotifyUpcoming(true);
        notificationSettings.setNotifyBeforeHours(10);

        UserEntity user = new UserEntity();
        user.setNotificationSettings(notificationSettings);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //when
        userService.deleteNotifications(1L);

        //assert
        NotificationSettings ntSettings = user.getNotificationSettings();

        assertThat(ntSettings.getNotifyNewEvents()).isFalse();
        assertThat(ntSettings.getNotifyUpcoming()).isTrue();
        assertThat(ntSettings.getNotifyBeforeHours()).isEqualTo(2);
    }

    @Test
    void deleteNotifications_whenNotificationSettingsIsDefault_shouldUserBookingChangeReminderAt(){
        //given
        EventEntity event = new EventEntity();
        event.setDateTime(Instant.parse("2099-12-07T10:30:00Z"));

        EventEntity event1 = new EventEntity();
        event1.setDateTime(Instant.parse("2199-12-07T10:30:00Z"));

        BookingEntity booking = new BookingEntity();
        booking.setEvent(event);

        BookingEntity booking1 = new BookingEntity();
        booking1.setEvent(event1);

        UserEntity user = new UserEntity();
        user.setBookings(Set.of(booking,booking1));

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //when
        userService.deleteNotifications(1L);

        //assert
        assertThat(booking.getReminderAt()).isEqualTo(Instant.parse("2099-12-07T08:30:00Z"));
        assertThat(booking1.getReminderAt()).isEqualTo(Instant.parse("2199-12-07T08:30:00Z"));
    }

    @Test
    void deleteNotifications_whenUserNotFound_returnUserNotFoundException(){
        //given
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //assert
        assertThatThrownBy(()->userService.deleteNotifications(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Ошибка! Зайдите в аккаунт еще раз!");
    }
}
