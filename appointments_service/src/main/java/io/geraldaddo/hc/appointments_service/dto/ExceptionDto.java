package io.geraldaddo.hc.appointments_service.dto;

import lombok.*;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExceptionDto {
    private String title;
    private String message;
    private HttpStatus status;
}
