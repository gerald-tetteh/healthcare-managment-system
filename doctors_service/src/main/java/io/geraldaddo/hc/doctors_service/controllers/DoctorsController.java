package io.geraldaddo.hc.doctors_service.controllers;

import io.geraldaddo.hc.doctors_service.dto.*;
import io.geraldaddo.hc.doctors_service.services.DoctorsService;
import io.geraldaddo.hc.user_data_module.entities.DoctorProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/doctors")
public class DoctorsController {
    @Autowired
    private DoctorsService doctorsService;

    @GetMapping("/{id}")
    @PreAuthorize("authentication.principal == #id || hasRole('ADMIN')")
    public ResponseEntity<DoctorProfileDto> getDoctorProfile(@PathVariable int id) {
        DoctorProfile profile = doctorsService.getProfile(id);
        return ResponseEntity.ok(buildDoctorProfileDto(profile));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<DoctorsAvailabilityDto> getAvailability(@PathVariable int id) {
        return ResponseEntity.ok(doctorsService.getAvailability(id, LocalDateTime.now()));
    }

    @GetMapping("/{id}/available/{date}")
    public ResponseEntity<DoctorAvailableDto> getIsDoctorAvailable(
            @PathVariable int id, @PathVariable LocalDateTime date) {
        return ResponseEntity.ok(doctorsService.getIsDoctorAvailable(id, date));
    }

    @PutMapping("/{id}")
    @PreAuthorize("authentication.principal == #id || hasRole('ADMIN')")
    public ResponseEntity<DoctorProfileDto> updateDoctorProfile(
            @PathVariable int id, @RequestBody UpdateDoctorProfileDto profileDto) {
        DoctorProfile updatedProfile = doctorsService.updateProfile(id, profileDto);
        return ResponseEntity.ok(buildDoctorProfileDto(updatedProfile));
    }

    private DoctorProfileDto buildDoctorProfileDto(DoctorProfile profile) {
        UserProfileDto userProfileDto = UserProfileDto.builder()
                .firstName(profile.getUserProfile().getFirstName())
                .lastName(profile.getUserProfile().getLastName())
                .age(profile.getUserProfile().getAge())
                .number(profile.getUserProfile().getNumber())
                .email(profile.getUserProfile().getEmail())
                .dateOfBirth(profile.getUserProfile().getDateOfBirth())
                .addressLineOne(profile.getUserProfile().getAddressLineOne())
                .addressLineTwo(profile.getUserProfile().getAddressLineTwo())
                .city(profile.getUserProfile().getCity())
                .county(profile.getUserProfile().getCounty())
                .country(profile.getUserProfile().getCountry())
                .emergencyFirstName(profile.getUserProfile().getEmergencyFirstName())
                .emergencyLastName(profile.getUserProfile().getEmergencyLastName())
                .emergencyNumber(profile.getUserProfile().getEmergencyNumber())
                .postCode(profile.getUserProfile().getPostCode())
                .joined(profile.getUserProfile().getJoined())
                .insuranceNumber(profile.getUserProfile().getInsuranceNumber())
                .sex(profile.getUserProfile().getSex())
                .build();
        return DoctorProfileDto.builder()
                .consultationFee(profile.getConsultationFee())
                .licenseNumber(profile.getLicenseNumber())
                .specialisation(profile.getSpecialisation())
                .availabilityList(profile.getAvailabilityList())
                .userProfile(userProfileDto)
                .build();
    }
}
