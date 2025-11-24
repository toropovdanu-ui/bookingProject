package com.skillbox.web.contoller;

import com.skillbox.security.AppUserDetails;
import com.skillbox.service.BookingService;
import com.skillbox.web.dto.booking.BookingResponse;
import com.skillbox.web.dto.booking.CreateBookingRequest;
import com.skillbox.web.dto.booking.UpdateBookingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        bookingService.deleteBooking(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getUserBookings(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(bookingService.getUserBookings(userDetails.getUserId()));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody CreateBookingRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(userDetails.getUserId(), request));
    }
}
