package com.skillbox.notification;

import com.skillbox.entity.BookingEntity;
import com.skillbox.repository.BookingRepository;
import com.skillbox.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationScheduler {
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void processDueReminders(){
        List<BookingEntity> bookings = bookingRepository.findBookingsWithReminder(
                Instant.now(),
                Instant.now().minus(5, ChronoUnit.MINUTES)
        );

        notificationService.sendUpcomingEventNotifications(bookings);
    }
}
