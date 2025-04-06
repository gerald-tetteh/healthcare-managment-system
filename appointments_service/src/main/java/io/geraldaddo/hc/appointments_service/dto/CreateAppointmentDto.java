package io.geraldaddo.hc.appointments_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CreateAppointmentDto {
    private Integer doctorId;
    private Integer patientId;
    private LocalDateTime dateTime;
    private String notes;
}
