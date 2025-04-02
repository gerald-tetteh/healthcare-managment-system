package io.geraldaddo.hc.doctors_service.dto;

import io.geraldaddo.hc.user_data_module.entities.Availability;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileDto {
    private String licenseNumber;
    private String specialisation;
    private double consultationFee;
    private UserProfileDto userProfile;
    private List<Availability> availabilityList;
}
