package com.skillbox.service;

import com.skillbox.entity.BookingEntity;
import com.skillbox.entity.EventEntity;
import com.skillbox.entity.UserEntity;
import com.skillbox.event.BookingConfirmedEvent;
import com.skillbox.event.EventCreatedEvent;
import com.skillbox.repository.BookingRepository;
import com.skillbox.repository.UserRepository;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.time.Instant;
import java.util.Properties;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private JavaMailSender javaMailSender;

    private String email = "toropov.danu@gmail.com";

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendUpcomingEventNotifications_whenBookingConditionAreCompleted_shouldSendNotifications() throws MessagingException, IOException {
        //given
        EventEntity event = new EventEntity();
        event.setDateTime(Instant.parse("2199-12-07T10:30:00Z"));

        UserEntity user = new UserEntity();
        user.setEmail("example@gmail.com");

        BookingEntity booking = new BookingEntity();
        booking.setEvent(event);
        booking.setUser(user);

        MimeMessage mimeMessage =
                new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        doNothing().when(bookingRepository).updateReminderSentToTrue(any());

        //when
        notificationService.processSingleBookingReminder(booking);

        //assert
        InternetAddress recipient = (InternetAddress) mimeMessage.getRecipients(Message.RecipientType.TO)[0];

        assertThat(mimeMessage.getSubject()).isEqualTo("Уведомление о предстоящем мероприятии");
        assertThat(mimeMessage.getFrom()[0].toString()).isEqualTo(email);
        assertThat(recipient.getAddress()).isEqualTo("example@gmail.com");
        assertThat((String) mimeMessage.getContent())
                .isEqualTo("Здравствуйте! Уведомляем вас, что по вашему бронированию состоится мероприятие 07.12.2199 в 13:30.");
    }

    @Test
    void sendCreateEventNotifications_whenUserIsSubscribedOnNewEvent_shouldSendNotifications() throws MessagingException, IOException {
        //given
        List<String> subscribedEmails = List.of("example@gmail.com", "example@mail.ru");

        when(userRepository.findSubscribedUserEmails()).thenReturn(subscribedEmails);
        when(javaMailSender.createMimeMessage())
                .thenAnswer(invocation->
                        new MimeMessage(Session.getDefaultInstance(new Properties())));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        //when
        notificationService.sendCreateEventNotifications(new EventCreatedEvent("test"));

        //assert
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);

        verify(javaMailSender, times(2)).send(captor.capture());

        List<MimeMessage> allValues = captor.getAllValues();

        MimeMessage message = allValues.get(0);
        MimeMessage message1 = allValues.get(1);

        InternetAddress recipient = (InternetAddress) message.getRecipients(Message.RecipientType.TO)[0];

        assertThat(message.getSubject()).isEqualTo("Уведомление о новом событии");
        assertThat(message.getFrom()[0].toString()).isEqualTo(email);
        assertThat(recipient.getAddress()).isEqualTo("example@gmail.com");
        assertThat((String) message.getContent())
                .isEqualTo("Уведомляем вас, что было создано новое мероприятие: test");

        InternetAddress recipient1 = (InternetAddress) message1.getRecipients(Message.RecipientType.TO)[0];

        assertThat(message1.getSubject()).isEqualTo("Уведомление о новом событии");
        assertThat(message1.getFrom()[0].toString()).isEqualTo(email);
        assertThat(recipient1.getAddress()).isEqualTo("example@mail.ru");
        assertThat((String) message1.getContent())
                .isEqualTo("Уведомляем вас, что было создано новое мероприятие: test");
    }

    @Test
    void sendBookingConfirmedNotification_whenAdminConfirmedBooking_shouldSendNotification() throws MessagingException, IOException {
        //given
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        //when
        notificationService.sendBookingConfirmedNotification(new BookingConfirmedEvent("example@gmail.com","test"));

        //assert
        InternetAddress recipient = (InternetAddress) mimeMessage.getRecipients(Message.RecipientType.TO)[0];

        assertThat(mimeMessage.getSubject()).isEqualTo("Ваше бронирование на мероприятие test подтверждено");
        assertThat(mimeMessage.getFrom()[0].toString()).isEqualTo(email);
        assertThat(recipient.getAddress()).isEqualTo("example@gmail.com");
        assertThat((String) mimeMessage.getContent())
                .isEqualTo("Ваше бронирование на мероприятие test подтвердил администратор");
    }
}
