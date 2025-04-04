package io.geraldaddo.hc.appointments_service.exception_handlers;

import io.geraldaddo.hc.appointments_service.dto.ExceptionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDto> generalExceptionHandler(Exception ex) {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .title("An error occurred on the server")
                .message(ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        return ResponseEntity.status(exceptionDto.getStatus()).body(exceptionDto);
    }
}
