package com.skillbox.web.contoller;

import com.skillbox.service.BookingService;
import com.skillbox.service.EventService;
import com.skillbox.web.dto.booking.BookingFilterRequest;
import com.skillbox.web.dto.booking.BookingResponse;
import com.skillbox.web.dto.event.EventResponse;
import com.skillbox.web.dto.event.UpsertEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final EventService eventService;
    private final BookingService bookingService;

    @PutMapping("/events/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id,
                                                     @RequestBody UpsertEventRequest request){
        return ResponseEntity.ok(eventService.updateEvent(id,request));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id){
        eventService.deleteEvent(id);

        return ResponseEntity.noContent()
                .build();
    }

    @PutMapping("/bookings/{id}/confirm")
    public ResponseEntity<Void> confirmBooking(@PathVariable Long id){
        bookingService.confirmBooking(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(@RequestBody UpsertEventRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(request));
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<BookingResponse>> getBookings(@ModelAttribute BookingFilterRequest filter,
                                                             Pageable pageable){
        return ResponseEntity.ok(
                bookingService.getAll(filter,pageable)
        );
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id){
        bookingService.deleteBooking(id);

        return ResponseEntity.noContent().build();
    }
}
