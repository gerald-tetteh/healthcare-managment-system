package io.geraldaddo.hc.gateway.exception_handlers;

import io.geraldaddo.hc.gateway.dtos.ExceptionResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthExceptionHandler {
    private final Logger logger = LogManager.getLogger(AuthExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponseDto> handleAuthenticationException(AuthenticationException ex) {
        ExceptionResponseDto responseDto = new ExceptionResponseDto("Authentication failed", ex.getMessage(), HttpStatus.UNAUTHORIZED);
        logger.error("Authentication failed:", ex);
        return ResponseEntity.status(responseDto.statusCode()).body(responseDto);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ExceptionResponseDto> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ExceptionResponseDto responseDto = new ExceptionResponseDto("Authorisation failed", ex.getMessage(), HttpStatus.UNAUTHORIZED);
        logger.error("Authorization failed:", ex);
        return ResponseEntity.status(responseDto.statusCode()).body(responseDto);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ExceptionResponseDto> handleExpiredJwtException(ExpiredJwtException ex) {
        ExceptionResponseDto responseDto = new ExceptionResponseDto("Token expired", ex.getMessage(), HttpStatus.BAD_REQUEST);
        logger.error("Token expired:", ex);
        return ResponseEntity.status(responseDto.statusCode()).body(responseDto);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ExceptionResponseDto> handleJwtException(JwtException ex) {
        ExceptionResponseDto responseDto = new ExceptionResponseDto("Could not parse or validated token", ex.getMessage(), HttpStatus.BAD_REQUEST);
        logger.error("Could not parse or validated token:", ex);
        return ResponseEntity.status(responseDto.statusCode()).body(responseDto);
    }
}
