package io.geraldaddo.hc.gateway.exception_handlers;

import io.geraldaddo.hc.gateway.dtos.ExceptionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleGeneralException(Exception ex) {
        ExceptionResponseDto responseDto = new ExceptionResponseDto(
                "Error occurred on server", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(responseDto.statusCode()).body(responseDto);
    }
}
