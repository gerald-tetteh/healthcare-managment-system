package io.geraldaddo.hc.gateway.exception_handlers;

import io.geraldaddo.hc.gateway.dtos.ExceptionResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponseDto> handleAuthenticationException(AuthenticationException ex) {
        ExceptionResponseDto responseDto = new ExceptionResponseDto("Authentication failed", ex.getMessage(), HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(responseDto.statusCode()).body(responseDto);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ExceptionResponseDto> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ExceptionResponseDto responseDto = new ExceptionResponseDto("Authorisation failed", ex.getMessage(), HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(responseDto.statusCode()).body(responseDto);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ExceptionResponseDto> handleExpiredJwtException(ExpiredJwtException ex) {
        ExceptionResponseDto responseDto = new ExceptionResponseDto("Token expired", ex.getMessage(), HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(responseDto.statusCode()).body(responseDto);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ExceptionResponseDto> handleJwtException(JwtException ex) {
        ExceptionResponseDto responseDto = new ExceptionResponseDto("Could not parse or validated token", ex.getMessage(), HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(responseDto.statusCode()).body(responseDto);
    }
}
