package io.geraldaddo.hc.appointments_service.exception_handlers;

import io.geraldaddo.hc.appointments_service.dto.ExceptionDto;
import io.geraldaddo.hc.appointments_service.exceptions.AppointmentsServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppointmentsControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDto> illegalArgumentHandler(IllegalArgumentException ex) {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .title("Unexpected input data")
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return ResponseEntity.status(exceptionDto.getStatus()).body(exceptionDto);
    }
    @ExceptionHandler(AppointmentsServerException.class)
    public ResponseEntity<ExceptionDto> appointmentsServerExceptionHandler(AppointmentsServerException ex) {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .title("Unexpected input data")
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return ResponseEntity.status(exceptionDto.getStatus()).body(exceptionDto);
    }
}
