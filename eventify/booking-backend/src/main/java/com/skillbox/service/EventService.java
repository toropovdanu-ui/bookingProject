package com.skillbox.service;

import com.skillbox.entity.EventEntity;
import com.skillbox.event.EventCreatedEvent;
import com.skillbox.mapper.EventMapper;
import com.skillbox.repository.EventRepository;
import com.skillbox.specification.EventSpecification;
import com.skillbox.web.dto.event.EventFilterRequest;
import com.skillbox.web.dto.event.EventResponse;
import com.skillbox.web.dto.event.UpsertEventRequest;
import com.skillbox.web.exception.EventNotFoundException;
import com.skillbox.web.exception.EventStartInPastException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventMapper eventMapper;
    private final ApplicationEventPublisher publisher;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public Page<EventResponse> findAll(EventFilterRequest filter,
                                             Pageable pageable){
        Page<EventEntity> eventsEntity = eventRepository.findAll(
                EventSpecification.withFilter(filter),
                pageable
        );

        List<EventResponse> dtos = eventsEntity.getContent().stream()
                .map(eventMapper::toDto)
                .toList();

        return new PageImpl<>(dtos,pageable,eventsEntity.getTotalElements());
    }

    @Transactional(readOnly = true)
    public EventResponse findById(Long eventId){
        EventEntity eventEntity = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Ошибка! Мероприятие не найдено!"));

        return eventMapper.toDto(eventEntity);
    }

    @Transactional
    public EventResponse createEvent(UpsertEventRequest request){
        EventEntity entity = eventMapper.toEntity(request);

        if (!entity.getDateTime().isAfter(Instant.now())) {
            throw new EventStartInPastException("Дата начала мероприятия не может быть меньше, чем время в данный момент");
        }

        entity.setAvailableTickets(request.getTotalTickets());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        EventEntity savedEvent = eventRepository.save(entity);

        publisher.publishEvent(new EventCreatedEvent(savedEvent.getTitle()));

        return eventMapper.toDto(savedEvent);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId,UpsertEventRequest request){
        EventEntity eventEntity = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Ошибка! Мероприятие не найдено!"));

        eventEntity.updateFrom(request);

        return eventMapper.toDto(eventEntity);
    }

    @Transactional
    public void deleteEvent(Long id){
        eventRepository.deleteById(id);
    }
}
