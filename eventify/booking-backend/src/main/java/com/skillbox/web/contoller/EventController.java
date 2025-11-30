package com.skillbox.web.contoller;

import com.skillbox.service.EventService;
import com.skillbox.web.dto.event.EventFilterRequest;
import com.skillbox.web.dto.event.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping
    @Operation(security = {})
    public ResponseEntity<Page<EventResponse>> getAll(@ModelAttribute EventFilterRequest filter,
                                  Pageable pageable){
        return ResponseEntity.ok(
                eventService.findAll(filter,pageable)
        );
    }

    @GetMapping("{id}")
    @Operation(security = {})
    public ResponseEntity<EventResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(
                eventService.findById(id)
        );
    }
}
