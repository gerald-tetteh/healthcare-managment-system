package io.geraldaddo.hc.doctors_service.controllers;

import io.geraldaddo.hc.doctors_service.services.DoctorsService;
import io.geraldaddo.hc.user_data_module.entities.Availability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorsController {
    @Autowired
    private DoctorsService doctorsService;

    @GetMapping
    public String helloWorld() {
        return "Hello World";
    }

    @GetMapping("/{id}/availability")
    @PreAuthorize("authentication.principal == #id || hasRole('ADMIN')")
    public ResponseEntity<List<Availability>> getAvailability(@PathVariable int id) {
        List<Availability> availabilities = doctorsService.getAvailability(id);
        return ResponseEntity.ok(availabilities);
    }
}
