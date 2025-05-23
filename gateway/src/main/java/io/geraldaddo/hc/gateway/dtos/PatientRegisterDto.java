package io.geraldaddo.hc.gateway.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.geraldaddo.hc.user_data_module.enums.Sex;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientRegisterDto(
        String firstName,
        String lastName,
        Integer age,
        @JsonFormat(pattern="yyyy-MM-dd") LocalDate dateOfBirth,
        String number,
        String emergencyFirstName,
        String emergencyLastName,
        String emergencyNumber,
        String password,
        String email,
        String addressLineOne,
        String addressLineTwo,
        String county,
        String insuranceNumber,
        Sex sex,
        String city,
        String postCode,
        String country,
        @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime joined) {
}
