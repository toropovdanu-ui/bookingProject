package com.skillbox.service;

import com.skillbox.entity.BookingEntity;
import com.skillbox.entity.EventEntity;
import com.skillbox.entity.UserEntity;
import com.skillbox.event.BookingConfirmedEvent;
import com.skillbox.mapper.BookingMapper;
import com.skillbox.mapper.EventMapper;
import com.skillbox.repository.BookingRepository;
import com.skillbox.repository.EventRepository;
import com.skillbox.repository.UserRepository;
import com.skillbox.specification.BookingSpecification;
import com.skillbox.web.dto.booking.BookingFilterRequest;
import com.skillbox.web.dto.booking.BookingResponse;
import com.skillbox.web.dto.booking.CreateBookingRequest;
import com.skillbox.web.dto.booking.UpdateBookingRequest;
import com.skillbox.web.dto.event.EventResponse;
import com.skillbox.web.exception.BookingNotFoundException;
import com.skillbox.web.exception.EventNotFoundException;
import com.skillbox.web.exception.InsufficientActivitiesException;
import com.skillbox.web.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    private final BookingMapper bookingMapper;
    private final EventMapper eventMapper;

    private final ApplicationEventPublisher publisher;

    @Transactional
    public void confirmBooking(Long id){
        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Ошибка! Бронирование не найдено"));

        bookingEntity.setConfirmed(true);

        publisher.publishEvent(new BookingConfirmedEvent(
                bookingEntity.getUser().getEmail(),
                bookingEntity.getEvent().getTitle()
        ));
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAll(BookingFilterRequest filter,
                                        Pageable pageable){
        Page<BookingEntity> entities = bookingRepository.findAll(
                BookingSpecification.withFilter(filter),
                pageable
        );

        List<BookingResponse> dtos = entities.getContent().stream()
                .map(this::getBookingDto)
                .toList();

        return new PageImpl<>(dtos, pageable, entities.getTotalElements());
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id){
        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Ошибка! Бронирование не найдено"));

        return getBookingDto(bookingEntity);
    }

    @Transactional
    public BookingResponse createBooking(Long userId,CreateBookingRequest bookingRequest){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ошибка! Зайдите в аккаунт еще раз!"));

        EventEntity eventEntity = eventRepository.findById(bookingRequest.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Ошибка! Мероприятие не найдено!"));

        int availableTickets = eventEntity.getAvailableTickets() - bookingRequest.getTicketCount();

        if(availableTickets < 0){
            throw new InsufficientActivitiesException("Нельзя забронировать " + bookingRequest.getTicketCount() +
                    " билетов: доступно только " + eventEntity.getAvailableTickets());
        }

        eventRepository.reduceAvailableTickets(eventEntity.getId(),bookingRequest.getTicketCount());

        EventEntity event = eventRepository.findById(eventEntity.getId())
                .orElseThrow();

        BookingEntity bookingEntity = getBookingEntity(event, userEntity, bookingRequest);

        return getBookingDto(bookingRepository.save(bookingEntity));
    }

    @Transactional
    public BookingResponse updateById(Long id, UpdateBookingRequest request) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено"));

        int oldCount = booking.getTicketCount();
        int newCount = request.getTicketCount();
        int delta = oldCount - newCount;

        EventEntity event = booking.getEvent();
        if (event.getAvailableTickets() + delta < 0) {
            throw new InsufficientActivitiesException("Недостаточно доступных билетов");
        }

        eventRepository.updateAvailableTickets(event.getId(), delta);
        booking.setTicketCount(newCount);
        bookingRepository.save(booking);

        return getBookingDto(booking);
    }

    @Transactional
    public void deleteBooking(Long id){
        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Ошибка! Бронирование не найдено"));

        eventRepository.updateAvailableTickets(
                bookingEntity.getEvent().getId(),
                bookingEntity.getTicketCount()
        );

        bookingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId){
        List<BookingEntity> bookingEntities = bookingRepository.findAllByUserId(userId);

        return bookingEntities.stream()
                .map(this::getBookingDto)
                .toList();
    }

    private BookingEntity getBookingEntity(EventEntity eventEntity, UserEntity userEntity,
                                           CreateBookingRequest bookingRequest) {
        Instant threeDaysLater = Instant.now().plus(3, ChronoUnit.DAYS);
        Instant expireDateTime = eventEntity.getDateTime().isBefore(threeDaysLater) ? eventEntity.getDateTime() : threeDaysLater;

        Integer notifyBeforeHours = userEntity.getNotificationSettings().getNotifyBeforeHours();
        Instant remainderAt = notifyBeforeHours == null && !userEntity.getNotificationSettings().getNotifyUpcoming()
                ? null : eventEntity.getDateTime().minus(notifyBeforeHours * 60, ChronoUnit.MINUTES);

        boolean remainderSent = true;
        if (notifyBeforeHours != null && notifyBeforeHours > 0) {
            Duration timeUntilEvent = Duration.between(Instant.now(), eventEntity.getDateTime());
            remainderSent = timeUntilEvent.minus(Duration.ofHours(notifyBeforeHours)).isNegative();
        }

        return new BookingEntity(userEntity, eventEntity, bookingRequest.getTicketCount(),
                Instant.now(), expireDateTime,remainderAt,remainderSent, false);
    }

    private BookingResponse getBookingDto(BookingEntity bookingEntity) {
        BookingResponse bookingDto = bookingMapper.toDto(bookingEntity);
        EventResponse eventDto = eventMapper.toDto(bookingEntity.getEvent());

        bookingDto.setEvent(eventDto);
        bookingDto.setCustomerEmail(bookingEntity.getUser().getEmail());

        return bookingDto;
    }
}
