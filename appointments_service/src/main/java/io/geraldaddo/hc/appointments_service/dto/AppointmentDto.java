package io.geraldaddo.hc.appointments_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentDto {
    private Integer appointmentId;
    private Integer doctorId;
    private Integer patientId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}