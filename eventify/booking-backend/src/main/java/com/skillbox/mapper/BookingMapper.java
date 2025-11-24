package com.skillbox.mapper;

import com.skillbox.entity.BookingEntity;
import com.skillbox.utils.DateTimeUtils;
import com.skillbox.web.dto.booking.BookingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "event", ignore = true)
    @Mapping(target = "customerEmail", ignore = true)
    BookingResponse toDto(BookingEntity entity);

    default Instant mapDateTime(String dateTime) {
        return DateTimeUtils.parseDate(dateTime);
    }

    default String mapToString(Instant dateTime){
        return dateTime != null ? dateTime.toString() : null;
    }
}
