package io.geraldaddo.hc.patients_service.controllers;

import io.geraldaddo.hc.patients_service.dtos.UserProfileDto;
import io.geraldaddo.hc.patients_service.services.PatientsService;
import io.geraldaddo.hc.user_data_module.entities.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patients")
public class PatientsController {
    private final Logger logger = LogManager.getLogger(PatientsController.class);

    @Autowired
    private PatientsService patientsService;

    @GetMapping("/{id}")
    @PreAuthorize("authentication.principal == #id || hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable int id) {
        User user = patientsService.getUserById(id);
        UserProfileDto dto = new UserProfileDto()
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setAge(user.getAge())
                .setNumber(user.getNumber())
                .setEmail(user.getEmail())
                .setDateOfBirth(user.getDateOfBirth())
                .setAddressLineOne(user.getAddressLineOne())
                .setAddressLineTwo(user.getAddressLineTwo())
                .setCity(user.getCity())
                .setCounty(user.getCounty())
                .setCountry(user.getCountry())
                .setEmergencyFirstName(user.getEmergencyFirstName())
                .setEmergencyLastName(user.getEmergencyLastName())
                .setEmergencyNumber(user.getEmergencyNumber())
                .setPostCode(user.getPostCode())
                .setJoined(user.getJoined())
                .setInsuranceNumber(user.getInsuranceNumber());
        return ResponseEntity.ok(dto);
    }
}
