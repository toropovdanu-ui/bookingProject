package com.skillbox.web.contoller;

import com.skillbox.service.BookingService;
import com.skillbox.web.dto.booking.BookingResponse;
import com.skillbox.web.dto.booking.UpdateBookingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateById(@PathVariable Long id,
                                                      @RequestBody UpdateBookingRequest request){
        return ResponseEntity.ok(bookingService.updateById(id,request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelById(@PathVariable Long id){


        return ResponseEntity.noContent().build();
    }
}
