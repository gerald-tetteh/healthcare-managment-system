package io.geraldaddo.hc.doctors_service.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorAvailableDto {
    public boolean isAvailable;
}
