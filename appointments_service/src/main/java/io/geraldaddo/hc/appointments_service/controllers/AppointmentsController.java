package io.geraldaddo.hc.appointments_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
public class AppointmentsController {

    @GetMapping
    public String helloWorld() {
        return "Hello World from appointments";
    }
}
