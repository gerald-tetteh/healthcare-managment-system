package io.geraldaddo.hc.patients_service.dtos;

import org.springframework.http.HttpStatus;

public record ExceptionResponseDto(String title, String message, HttpStatus statusCode) {
}
