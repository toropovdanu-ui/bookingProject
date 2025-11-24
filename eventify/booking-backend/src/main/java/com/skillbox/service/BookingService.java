package com.skillbox.service;

import com.skillbox.entity.BookingEntity;
import com.skillbox.entity.EventEntity;
import com.skillbox.entity.UserEntity;
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
import com.skillbox.web.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.process.internal.UserTypeResolution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    private final BookingMapper bookingMapper;
    private final EventMapper eventMapper;

    @Transactional
    public void confirmBooking(Long id){
        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Ошибка! Бронирование не найдено"));

        bookingEntity.setConfirmed(true);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAll(BookingFilterRequest filter,
                                        Pageable pageable){
        Page<BookingEntity> entities = bookingRepository.findAll(
                BookingSpecification.withFilter(filter),
                pageable
        );

        List<BookingResponse> dtos = entities.getContent().stream()
                .map(entity -> {
                    BookingResponse bookingDto = bookingMapper.toDto(entity);
                    EventResponse eventDto = eventMapper.toDto(entity.getEvent());

                    bookingDto.setEvent(eventDto);
                    bookingDto.setCustomerEmail(entity.getUser().getEmail());

                    return bookingDto;
                })
                .toList();

        return new PageImpl<>(dtos, pageable, entities.getTotalElements());
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id){
        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Ошибка! Бронирование не найдено"));

        BookingResponse bookingDto = bookingMapper.toDto(bookingEntity);
        EventResponse eventDto = eventMapper.toDto(bookingEntity.getEvent());

        bookingDto.setEvent(eventDto);
        bookingDto.setCustomerEmail(bookingEntity.getUser().getEmail());

        return bookingDto;
    }

    @Transactional
    public BookingResponse createBooking(Long userId,CreateBookingRequest request){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ошибка! Зайдите в аккаунт еще раз!"));

        EventEntity eventEntity = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Ошибка! Мероприятие не найдено!"));

        long minTimeForExpireTime = Math.min(
                eventEntity.getDateTime().getEpochSecond(),
                Instant.now().plus(3, ChronoUnit.DAYS).getEpochSecond()
        );

        Instant expireDateTime = Instant.ofEpochMilli(minTimeForExpireTime);

        BookingEntity bookingEntity = new BookingEntity(userEntity, eventEntity, request.getTicketCount(),
                Instant.now(), expireDateTime, false);

        BookingEntity savedBooking = bookingRepository.save(bookingEntity);

        BookingResponse bookingDto = bookingMapper.toDto(savedBooking);
        EventResponse eventDto = eventMapper.toDto(savedBooking.getEvent());

        bookingDto.setEvent(eventDto);
        bookingDto.setCustomerEmail(bookingEntity.getUser().getEmail());

        return bookingDto;
    }

    @Transactional
    public BookingResponse updateById(Long id,UpdateBookingRequest request){
        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Ошибка! Бронирование не найдено"));

        bookingEntity.setTicketCount(request.getTicketCount());

        return bookingMapper.toDto(bookingEntity);
    }

    @Transactional
    public void deleteBooking(Long id){
        bookingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId){
        List<BookingEntity> bookingEntities = bookingRepository.findAllByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Ошибка! Зайдите в аккаунт еще раз!"));

        return bookingEntities.stream()
                .map(entity -> {
                    BookingResponse bookingDto = bookingMapper.toDto(entity);
                    EventResponse eventDto = eventMapper.toDto(entity.getEvent());

                    bookingDto.setEvent(eventDto);
                    bookingDto.setCustomerEmail(entity.getUser().getEmail());

                    return bookingDto;
                })
                .toList();
    }
}
