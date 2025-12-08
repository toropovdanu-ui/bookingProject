package com.skillbox.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.concurrent.ThreadPoolExecutor;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ApiError> userExistsException(UserExistsException e){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        "USER_EXISTS",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiError> bookingNotFoundException(BookingNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        "BOOKING_NOT_FOUND",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> userNotFoundException(UserNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        "USER_NOT_FOUND",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiError> eventNotFoundException(EventNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        "EVENT_NOT_FOUND",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(EventStartInPastException.class)
    public ResponseEntity<ApiError> eventStartInPastException(EventStartInPastException e){
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError(
                        "EVENT_IN_PAST_TIME",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(InsufficientActivitiesException.class)
    public ResponseEntity<ApiError> insufficientActivitiesException(InsufficientActivitiesException e){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        new ApiError(
                                "TICKETS_NOT_AVAILABLE",
                                e.getMessage()
                        )
                );
    }

    @ExceptionHandler(DateTimeFormatException.class)
    public ResponseEntity<ApiError> dateTimeFormatException(DateTimeFormatException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        new ApiError(
                                "DATE_TIME_FORMAT",
                                e.getMessage()
                        )
                );

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> methodArgumentNotValidException(MethodArgumentNotValidException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        new ApiError(
                                "VALIDATION_ERROR",
                                e.getMessage()
                        )
                );

    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> invalidRequestException(InvalidRequestException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        new ApiError(
                                "INVALID_REQUEST",
                                e.getMessage()
                        )
                );

    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accessDeniedException(AccessDeniedException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        new ApiError("UNAUTHORIZED",
                                "Авторизуйтесь еще раз")
                );
    }

    @ExceptionHandler(InsufficientTotalTicketsException.class)
    public ResponseEntity<ApiError> insufficientTotalTicketsException(InsufficientTotalTicketsException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        new ApiError("INSUFFICIENT_TOTAL_TICKETS",
                                e.getMessage())
                );
    }
}
