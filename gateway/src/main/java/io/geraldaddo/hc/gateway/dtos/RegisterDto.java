package io.geraldaddo.hc.gateway.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegisterDto(
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
        String city,
        String postCode,
        String country,
        @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime joined) {
}
