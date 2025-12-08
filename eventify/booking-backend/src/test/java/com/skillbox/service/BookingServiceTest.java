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
import com.skillbox.web.dto.booking.BookingFilterRequest;
import com.skillbox.web.dto.booking.BookingResponse;
import com.skillbox.web.dto.booking.CreateBookingRequest;
import com.skillbox.web.dto.booking.UpdateBookingRequest;
import com.skillbox.web.dto.event.EventResponse;
import com.skillbox.web.exception.BookingNotFoundException;
import com.skillbox.web.exception.EventNotFoundException;
import com.skillbox.web.exception.InsufficientActivitiesException;
import com.skillbox.web.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private ApplicationEventPublisher publisher;

    private String email = "toropov.danu@gmail.com";

    @InjectMocks
    private BookingService bookingService;

    @Test
    void confirmBooking_whenBookingIsExists_shouldBookingMustBeConfirmed(){
        //given
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);

        EventEntity eventEntity = new EventEntity();
        eventEntity.setTitle("Java-топ");

        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setUser(userEntity);
        bookingEntity.setEvent(eventEntity);

        Optional<BookingEntity> booking = Optional.of(bookingEntity);

        when(bookingRepository.findById(any())).thenReturn(booking);

        ArgumentCaptor<BookingConfirmedEvent> captor = ArgumentCaptor.forClass(BookingConfirmedEvent.class);

        //when
        bookingService.confirmBooking(1L);

        //return
        verify(publisher).publishEvent(captor.capture());
        BookingConfirmedEvent value = captor.getValue();

        assertThat(value.getEmailUser()).isEqualTo(email);
        assertThat(value.getEventTitle()).isEqualTo("Java-топ");
    }

    @Test
    void confirmBooking_whenBookingIsNotFound_thenReturnException(){
        //given
        when(bookingRepository.findById(any())).thenReturn(Optional.empty());

        //return
        assertThatThrownBy(()->bookingService.confirmBooking(1L))
                .hasMessage("Ошибка! Бронирование не найдено")
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void getAll_whenRequestingWithPageAndSize_returnAllBookings(){
        //given
        BookingFilterRequest filter = new BookingFilterRequest();

        UserEntity user = new UserEntity();
        user.setEmail(email);

        EventEntity event = new EventEntity();
        event.setTitle("IT-топ");

        BookingEntity booking = new BookingEntity();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setCreatedAt(Instant.now());

        List<BookingEntity> entities = List.of(booking);

        Pageable mockPageable = PageRequest.of(0,10, Sort.by(
                Sort.Order.asc("createdAt")
        ));
        Page<BookingEntity> pageableBookings = new PageImpl<>(entities, mockPageable, 1);

        when(bookingRepository.findAll(any(Specification.class),any(Pageable.class)))
                .thenReturn(pageableBookings);
        when(bookingMapper.toDto(booking)).thenReturn(new BookingResponse());
        when(eventMapper.toDto(any())).thenReturn(new EventResponse());

        //when
        Page<BookingResponse> page = bookingService.getAll(filter, mockPageable);

        //return
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(10);
        assertThat(page.getContent().get(0).getCustomerEmail()).isEqualTo(email);
    }

    @Test
    void getById_whenBookingIsExists_returnBookingById(){
        //given
        UserEntity user = new UserEntity();
        user.setEmail(email);

        EventEntity event = new EventEntity();

        BookingEntity booking = new BookingEntity();
        booking.setEvent(event);
        booking.setUser(user);

        EventResponse eventResponse = new EventResponse();
        eventResponse.setTitle("example");

        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(any())).thenReturn(new BookingResponse());
        when(eventMapper.toDto(any())).thenReturn(eventResponse);

        //when
        BookingResponse bookingDTO = bookingService.getById(1L);

        //return
        assertThat(bookingDTO.getCustomerEmail()).isEqualTo(email);
        assertThat(bookingDTO.getEvent().getTitle()).isEqualTo("example");
    }

    @Test
    void getById_whenBookingIsNotExists_returnBookingNotFoundException(){
        //given
        when(bookingRepository.findById(any())).thenReturn(Optional.empty());

        //return
        assertThatThrownBy(()->bookingService.getById(1L))
                .hasMessage("Ошибка! Бронирование не найдено")
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void createBooking_whenAvailableTicketsAreMoreThanZero_shouldCreateBooking(){
        //given
        ArgumentCaptor<BookingEntity> captor = ArgumentCaptor.forClass(BookingEntity.class);
        CreateBookingRequest request = new CreateBookingRequest(1L, 5);

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);

        EventEntity eventEntity = new EventEntity();
        eventEntity.setId(1L);
        eventEntity.setAvailableTickets(10);
        eventEntity.setDateTime(Instant.parse("2025-12-07T10:30:00Z"));

        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setEvent(eventEntity);
        bookingEntity.setUser(userEntity);

        when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        when(eventRepository.findById(any())).thenReturn(Optional.of(eventEntity));
        doAnswer(invocation ->{
            int availableTickets = eventEntity.getAvailableTickets() - request.getTicketCount();
            eventEntity.setAvailableTickets(availableTickets);
            return null;
        }).when(eventRepository).reduceAvailableTickets(eq(1L), eq(5));
        when(bookingRepository.save(captor.capture())).thenReturn(bookingEntity);
        when(bookingMapper.toDto(any())).thenReturn(new BookingResponse());
        when(eventMapper.toDto(any())).thenReturn(new EventResponse());

        //when
        BookingResponse bookingDTO = bookingService.createBooking(1L, request);

        //return
        BookingEntity captureBooking = captor.getValue();

        assertThat(captureBooking.getEvent().getDateTime()).isEqualTo(Instant.parse("2025-12-07T10:30:00Z"));
        assertThat(captureBooking.getEvent().getAvailableTickets()).isEqualTo(5);
        assertThat(captureBooking.getUser().getEmail()).isEqualTo(email);

        assertThat(bookingDTO.getCustomerEmail()).isEqualTo(email);

        verify(eventRepository,times(2)).findById(any());
        verify(eventRepository,times(1)).reduceAvailableTickets(1L,5);
        verify(userRepository,times(1)).findById(any());
        verify(bookingRepository).save(any());
    }

    @Test
    void createBooking_whenUserNotFound_returnUserNotFoundException(){
        //given
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //return
        assertThatThrownBy(()->bookingService.createBooking(1L,new CreateBookingRequest()))
                .hasMessage("Ошибка! Зайдите в аккаунт еще раз!")
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createBooking_whenEventNotFound_returnEventNotFoundException(){
        //given
        CreateBookingRequest request = new CreateBookingRequest();
        request.setEventId(1L);

        when(userRepository.findById(any())).thenReturn(Optional.of(new UserEntity()));
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        //return
        assertThatThrownBy(()->bookingService.createBooking(1L,request))
                .hasMessage("Ошибка! Мероприятие не найдено!")
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void createBooking_whenNotEnoughAvailableTicket_returnInsufficientActivitiesException(){
        //given
        CreateBookingRequest request = new CreateBookingRequest();
        request.setEventId(1L);
        request.setTicketCount(10);

        EventEntity event = new EventEntity();
        event.setAvailableTickets(5);

        when(userRepository.findById(any())).thenReturn(Optional.of(new UserEntity()));
        when(eventRepository.findById(any())).thenReturn(Optional.of(event));

        //return
        assertThatThrownBy(()->bookingService.createBooking(1L,request))
                .hasMessage("Нельзя забронировать " + request.getTicketCount() +
                        " билетов: доступно только " + event.getAvailableTickets())
                .isInstanceOf(InsufficientActivitiesException.class);
    }

    @Test
    void updateById_whenTicketsAreAvailable_returnUpdatedBooking(){
        //given
        UserEntity user = new UserEntity();
        user.setEmail(email);

        EventEntity event = new EventEntity();
        event.setId(1L);
        event.setAvailableTickets(10);

        BookingEntity booking = new BookingEntity();
        booking.setTicketCount(3);
        booking.setEvent(event);
        booking.setUser(user);

        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setTicketCount(5);

        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
        doNothing().when(eventRepository).updateAvailableTickets(eq(1L),eq(-2));
        when(bookingRepository.save(any())).thenReturn(new BookingEntity());
        when(bookingMapper.toDto(booking)).thenReturn(new BookingResponse());
        when(eventMapper.toDto(event)).thenReturn(new EventResponse());

        //when
        BookingResponse bookingResponse = bookingService.updateById(1L, request);

        //return
        assertThat(bookingResponse.getCustomerEmail()).isEqualTo(email);

        verify(eventRepository, times(1)).updateAvailableTickets(1L,-2);
    }

    @Test
    void updateById_whenBookingNotFound_returnBookingNotFoundException(){
        //given
        when(bookingRepository.findById(any())).thenReturn(Optional.empty());

        //return
        assertThatThrownBy(()->bookingService.updateById(1L,new UpdateBookingRequest()))
                .hasMessage("Бронирование не найдено")
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void updateById_whenNotEnoughTicket_returnInsufficientActivitiesException(){
        //given
        EventEntity event = new EventEntity();
        event.setAvailableTickets(0);

        BookingEntity booking = new BookingEntity();
        booking.setTicketCount(3);
        booking.setEvent(event);

        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setTicketCount(5);

        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

        //return
        assertThatThrownBy(()->bookingService.updateById(1L,request))
                .isInstanceOf(InsufficientActivitiesException.class)
                .hasMessage("Недостаточно доступных билетов");
    }

    @Test
    void deleteBooking_whenBookingIsExists_thenDeleteById(){
        //given
        EventEntity event = new EventEntity();
        event.setId(1L);

        BookingEntity booking = new BookingEntity();
        booking.setEvent(event);
        booking.setTicketCount(5);

        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
        doNothing().when(eventRepository).updateAvailableTickets(eq(1L),eq(5));
        doNothing().when(bookingRepository).deleteById(any());

        //when
        bookingService.deleteBooking(1L);

        //assert
        verify(bookingRepository,times(1)).deleteById(eq(1L));
    }

    @Test
    void deleteBooking_whenBookingNotFound_returnBookingNotFoundException(){
        //given
        when(bookingRepository.findById(any())).thenReturn(Optional.empty());

        //assert
        assertThatThrownBy(()->bookingService.deleteBooking(1L))
                .hasMessage("Ошибка! Бронирование не найдено")
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void getUserBookings_whenUserIdIsExists_returnUserBookings(){
        //given
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);

        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setUser(userEntity);
        bookingEntity.setEvent(new EventEntity());

        BookingEntity bookingEntity1 = new BookingEntity();
        bookingEntity1.setUser(userEntity);
        bookingEntity1.setEvent(new EventEntity());

        List<BookingEntity> entities = List.of(bookingEntity, bookingEntity1);

        when(bookingRepository.findAllByUserId(any())).thenReturn(entities);
        when(bookingMapper.toDto(any())).thenReturn(new BookingResponse());
        when(eventMapper.toDto(any())).thenReturn(new EventResponse());

        //when
        List<BookingResponse> userBookings = bookingService.getUserBookings(1L);

        //assert
        assertThat(userBookings.size()).isEqualTo(2);
        assertThat(userBookings.get(0).getCustomerEmail()).isEqualTo(email);
        assertThat(userBookings.get(1).getCustomerEmail()).isEqualTo(email);
    }
}
