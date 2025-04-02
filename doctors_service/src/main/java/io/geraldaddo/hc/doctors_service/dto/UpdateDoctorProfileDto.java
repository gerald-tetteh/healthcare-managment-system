package io.geraldaddo.hc.doctors_service.dto;

import io.geraldaddo.hc.user_data_module.entities.Availability;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDoctorProfileDto {
    private String licenseNumber;
    private String specialisation;
    private double consultationFee;
    private List<Availability> availabilityList;
}
