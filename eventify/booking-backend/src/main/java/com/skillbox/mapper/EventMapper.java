package com.skillbox.mapper;

import com.skillbox.entity.EventEntity;
import com.skillbox.utils.DateTimeUtils;
import com.skillbox.web.dto.event.EventResponse;
import com.skillbox.web.dto.event.UpsertEventRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {
    EventResponse toDto(EventEntity entity);

    @Mapping(target = "dateTime", source = "dateTime")
    EventEntity toEntity(UpsertEventRequest request);

    default Instant mapDateTime(String dateTime) {
        return DateTimeUtils.parseDate(dateTime);
    }

    default String mapToString(Instant dateTime){
        return dateTime != null ? dateTime.toString() : null;
    }
}
