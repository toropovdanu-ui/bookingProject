package com.skillbox.service;

import com.skillbox.entity.BookingEntity;
import com.skillbox.mapper.BookingMapper;
import com.skillbox.mapper.EventMapper;
import com.skillbox.repository.BookingRepository;
import com.skillbox.specification.BookingSpecification;
import com.skillbox.web.dto.booking.BookingFilterRequest;
import com.skillbox.web.dto.booking.BookingResponse;
import com.skillbox.web.dto.booking.UpdateBookingRequest;
import com.skillbox.web.dto.event.EventResponse;
import com.skillbox.web.exception.BookingNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
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

}
