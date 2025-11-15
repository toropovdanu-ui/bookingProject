package com.skillbox.mapper;

import com.skillbox.entity.EventEntity;
import com.skillbox.web.dto.event.EventResponse;
import com.skillbox.web.dto.event.UpsertEventRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventResponse toDto(EventEntity entity);

    EventEntity toEntity(UpsertEventRequest request);
}
