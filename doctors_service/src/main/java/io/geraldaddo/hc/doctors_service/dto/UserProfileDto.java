package io.geraldaddo.hc.doctors_service.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserProfileDto {
    private String firstName;
    private String lastName;
    private Integer age;
    private LocalDate dateOfBirth;
    private String number;
    private String emergencyFirstName;
    private String emergencyLastName;
    private String emergencyNumber;
    private String email;
    private String addressLineOne;
    private String addressLineTwo;
    private String county;
    private String insuranceNumber;
    private String city;
    private String postCode;
    private String country;
    private LocalDateTime joined;
}
