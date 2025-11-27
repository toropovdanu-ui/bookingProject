package com.skillbox.specification;

import com.skillbox.entity.BookingEntity;
import com.skillbox.web.dto.booking.BookingFilterRequest;
import org.springframework.data.jpa.domain.Specification;

public interface BookingSpecification {
    static Specification<BookingEntity> withFilter(BookingFilterRequest filter){
        return Specification.where(byEventId(filter.getEventId()))
                .and(byUnconfirmedOnly(filter.getUnconfirmedOnly()));
    }

    static Specification<BookingEntity> byEventId(Long eventId) {
        return ((root, query, criteriaBuilder) -> {
            if(eventId == null){
                return null;
            }

            return criteriaBuilder.equal(root.get("user").get("id"),eventId);
        });
    }

    static Specification<BookingEntity> byUnconfirmedOnly(Boolean unconfirmedOnly) {
        return ((root, query, criteriaBuilder) -> {
            if(unconfirmedOnly == null){
                return null;
            }

            return criteriaBuilder.equal(root.get("confirmed"), unconfirmedOnly);
        });
    }
}
