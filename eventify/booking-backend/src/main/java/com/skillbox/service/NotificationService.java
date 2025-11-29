package com.skillbox.service;

import com.skillbox.entity.BookingEntity;
import com.skillbox.entity.EventEntity;
import com.skillbox.event.BookingConfirmedEvent;
import com.skillbox.event.EventCreatedEvent;
import com.skillbox.repository.BookingRepository;
import com.skillbox.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final Executor executor;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PlatformTransactionManager transactionManager;
    private final JavaMailSender javaMailSender;
    private final String from = "toropov.danu@gmail.com";

    public void sendUpcomingEventNotifications(List<BookingEntity> bookings){
        for(BookingEntity booking:bookings){
            executor.execute(()->{
                try {
                    MimeMessage message = javaMailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true);

                    helper.setFrom(from);
                    helper.setTo(booking.getUser().getEmail());
                    helper.setSubject("Уведомление о предстоящем мероприятии");
                    helper.setText(getText(booking.getEvent()));

                    javaMailSender.send(message);

                    new TransactionTemplate(transactionManager).execute(status -> {
                        bookingRepository.updateReminderSentToTrue(booking.getId());
                        return null;
                    });
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @EventListener(EventCreatedEvent.class)
    public void sendCreateEventNotifications(EventCreatedEvent event) {
        List<String> emails = userRepository.findSubscribedUserEmails();

        for(String email:emails){
            try {
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

                helper.setFrom(from);
                helper.setTo(email);
                helper.setSubject("Уведомление о новом событии");
                helper.setText("Уведомляем вас, что было создано новое мероприятие: " + event.getEventName());

                javaMailSender.send(mimeMessage);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventListener(BookingConfirmedEvent.class)
    public void sendBookingConfirmedNotification(BookingConfirmedEvent event){
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(from);
            helper.setTo(event.getEmailUser());
            helper.setSubject("Ваше бронирование на мероприятие " + event.getEventTitle() + " подтверждено");
            helper.setText("Ваше бронирование на мероприятие " + event.getEventTitle() + " подтвердил администратор");

            javaMailSender.send(message);
        }catch (MessagingException e){
            throw new RuntimeException(e);
        }
    }

    private String getText(EventEntity event) {
        Instant dateTime = event.getDateTime();
        ZoneId zone = ZoneId.of("Europe/Moscow");
        ZonedDateTime zdt = dateTime.atZone(zone);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm");
        String formattedDate = zdt.format(formatter);

        return "Здравствуйте! Уведомляем вас, что по вашему бронированию состоится мероприятие " + formattedDate + ".";
    }
}
