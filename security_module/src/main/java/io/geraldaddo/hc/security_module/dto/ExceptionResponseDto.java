package io.geraldaddo.hc.security_module.dto;

import org.springframework.http.HttpStatus;

public record ExceptionResponseDto(String title, String message, HttpStatus statusCode) {
}
