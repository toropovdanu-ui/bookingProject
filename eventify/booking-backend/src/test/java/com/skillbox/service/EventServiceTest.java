package com.skillbox.service;

import com.skillbox.entity.EventEntity;
import com.skillbox.event.EventCreatedEvent;
import com.skillbox.mapper.EventMapper;
import com.skillbox.repository.EventRepository;
import com.skillbox.web.dto.event.EventFilterRequest;
import com.skillbox.web.dto.event.EventResponse;
import com.skillbox.web.dto.event.UpsertEventRequest;
import com.skillbox.web.exception.EventNotFoundException;
import com.skillbox.web.exception.EventStartInPastException;
import com.skillbox.web.exception.InsufficientTotalTicketsException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private EventMapper eventMapper;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void findAll_whenEventListIsExists_returnNotEmptyPageList(){
        //given
        EventFilterRequest filter = new EventFilterRequest();

        List<EventEntity> entities = List.of(new EventEntity(), new EventEntity(), new EventEntity());

        Pageable pageable = PageRequest.of(0,10, Sort.by(
                Sort.Order.asc("dateTime")
        ));

        Page<EventEntity> entityPage = new PageImpl<>(entities,pageable,3);

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(eventMapper.toDto(any())).thenReturn(new EventResponse());

        //when
        Page<EventResponse> pageEventDto = eventService.findAll(filter, pageable);

        //assert
        assertThat(pageEventDto.getNumber()).isEqualTo(0);
        assertThat(pageEventDto.getTotalElements()).isEqualTo(3);
        assertThat(pageEventDto.getSize()).isEqualTo(10);
    }

    @Test
    void findById_whenEventIdIsExists_returnEventDto(){
        //given
        when(eventRepository.findById(any())).thenReturn(Optional.of(new EventEntity()));
        when(eventMapper.toDto(any())).thenReturn(new EventResponse());

        //when
        eventService.findById(1L);

        //assert
        verify(eventRepository, times(1)).findById(1L);
        verify(eventMapper,times(1)).toDto(any());
    }

    @Test
    void findById_whenEventDoesNotExists_returnEventNotFoundException(){
        //given
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        //assert
        assertThatThrownBy(()->eventService.findById(1L))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Ошибка! Мероприятие не найдено!");
    }

    @Test
    void createEvent_whenDataFromRequestIsValid_shouldCreateEvent(){
        //given
        ArgumentCaptor<EventEntity> captor = ArgumentCaptor.forClass(EventEntity.class);

        UpsertEventRequest request = new UpsertEventRequest();
        request.setTotalTickets(100);

        EventEntity event = new EventEntity();
        event.setDateTime(Instant.parse("2199-12-07T10:30:00Z"));

        EventEntity event1 = new EventEntity();
        event1.setTitle("test");

        when(eventMapper.toEntity(any())).thenReturn(event);
        when(eventRepository.save(captor.capture())).thenReturn(event1);
        when(eventMapper.toDto(any())).thenReturn(new EventResponse());

        //when
        eventService.createEvent(request);

        //assert
        EventEntity captureEvent = captor.getValue();

        assertThat(captureEvent.getAvailableTickets()).isEqualTo(100);
        assertThat(captureEvent.getDateTime()).isEqualTo(Instant.parse("2199-12-07T10:30:00Z"));

        verify(eventMapper,times(1)).toEntity(any());
        verify(eventRepository,times(1)).save(any());
        verify(publisher,times(1)).publishEvent(any(EventCreatedEvent.class));
        verify(eventMapper,times(1)).toDto(any());
    }

    @Test
    void createEvent_whenEventStartDataIsBeforeNowTime_returnEventStartInPastException(){
        //given
        EventEntity event = new EventEntity();
        event.setDateTime(Instant.parse("1980-12-07T10:30:00Z"));

        when(eventMapper.toEntity(any())).thenReturn(event);

        //assert
        assertThatThrownBy(()->eventService.createEvent(new UpsertEventRequest()))
                .isInstanceOf(EventStartInPastException.class)
                .hasMessage("Дата начала мероприятия не может быть меньше, чем время в данный момент");
    }

    @Test
    void updateEvent_whenDataFromRequestIsValid_returnUpdatedEvent(){
        //given
        ArgumentCaptor<EventEntity> captor = ArgumentCaptor.forClass(EventEntity.class);

        UpsertEventRequest request = new UpsertEventRequest();
        request.setTitle("it");
        request.setDescription("It-топ");
        request.setDateTime("2199-12-07T10:30:00Z");
        request.setTotalTickets(50);

        EventEntity event = new EventEntity();
        event.setTotalTickets(100);
        event.setAvailableTickets(50);

        when(eventRepository.findById(any())).thenReturn(Optional.of(event));
        when(eventRepository.save(captor.capture())).thenReturn(new EventEntity());
        when(eventMapper.toDto(any())).thenReturn(new EventResponse());

        //when
        eventService.updateEvent(1L,request);

        //assert
        EventEntity eventEntity = captor.getValue();

        assertThat(eventEntity.getTitle()).isEqualTo(request.getTitle());
        assertThat(eventEntity.getDescription()).isEqualTo(request.getDescription());
        assertThat(eventEntity.getDateTime()).isEqualTo(Instant.parse("2199-12-07T10:30:00Z"));
        assertThat(eventEntity.getTotalTickets()).isEqualTo(request.getTotalTickets());
        assertThat(eventEntity.getAvailableTickets()).isEqualTo(0);
    }

    @Test
    void updateEvent_whenEventNotFound_returnEventNotFoundException(){
        //given
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        //assert
        assertThatThrownBy(()->eventService.updateEvent(1L,new UpsertEventRequest()))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Ошибка! Мероприятие не найдено!");
    }

    @Test
    void updateEvent_whenInsufficientTotalTickets_returnInsufficientTotalTicketsException(){
        //given
        UpsertEventRequest request = new UpsertEventRequest();
        request.setTotalTickets(10);

        EventEntity event = new EventEntity();
        event.setTotalTickets(100);
        event.setAvailableTickets(50);

        when(eventRepository.findById(any())).thenReturn(Optional.of(event));

        //assert
        assertThatThrownBy(()->eventService.updateEvent(1L,request))
                .isInstanceOf(InsufficientTotalTicketsException.class)
                .hasMessage("Нельзя установить " + request.getTotalTickets() +
                        ", так как уже забронировано " + 50 + " билетов");
    }

    @Test
    void deleteEvent_shouldDeleteEvent(){
        //given
        doNothing().when(eventRepository).deleteById(any());

        //when
        eventService.deleteEvent(1L);

        //assert
        verify(eventRepository,times(1)).deleteById(1L);
    }
}
