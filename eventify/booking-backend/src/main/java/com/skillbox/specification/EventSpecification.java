package com.skillbox.specification;

import com.skillbox.entity.EventEntity;
import com.skillbox.utils.DateTimeUtils;
import com.skillbox.web.dto.event.EventFilterRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public interface EventSpecification {
    static Specification<EventEntity> withFilter(EventFilterRequest filter){
        return Specification.where(byDateInterval(
                DateTimeUtils.parseDate(filter.getFrom()),
                DateTimeUtils.parseDate(filter.getTo())
        ));
    }

    static Specification<EventEntity> byDateInterval(Instant from, Instant to) {
        return ((root, query, cb) -> {
            if(from == null && to == null){
                return null;
            }

            if(from == null){
                cb.lessThanOrEqualTo(root.get("start_date"),to);
            }

            if(to == null){
                cb.greaterThanOrEqualTo(root.get("start_date"),from);
            }

            return cb.between(root.get("start_date"),from,to);
        });
    }


}
