package io.geraldaddo.hc.appointments_service.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorAvailableDto {
    public boolean isAvailable;
}
