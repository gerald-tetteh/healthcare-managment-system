package io.geraldaddo.hc.patients_service.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patients")
public class PatientsController {
    private final Logger logger = LogManager.getLogger(PatientsController.class);

    @GetMapping
    @Secured("ROLE_PATIENT")
    public String helloWorld() {
        return "Hello from patients";
    }
}
