package io.geraldaddo.hc.patients_service.exception_handlers;

import io.geraldaddo.hc.patients_service.dtos.ExceptionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PatientsControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponseDto> handleAuthenticationException(IllegalArgumentException ex) {
        ExceptionResponseDto responseDto = new ExceptionResponseDto("Unexpected argument", ex.getMessage(), HttpStatus.NOT_FOUND);
        return ResponseEntity.status(responseDto.statusCode()).body(responseDto);
    }
}
