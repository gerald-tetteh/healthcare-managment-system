package io.geraldaddo.hc.doctors_service.dto;

import io.geraldaddo.hc.doctors_service.entities.CurrentStatus;
import io.geraldaddo.hc.user_data_module.entities.Availability;
import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class DoctorsAvailabilityDto {
    private List<Availability> availability;
    private CurrentStatus currentStatus;
}
