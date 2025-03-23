package io.geraldaddo.hc.gateway.dtos;

import org.springframework.http.HttpStatus;

public record ExceptionResponseDto(String title, String message, HttpStatus statusCode) {
}
