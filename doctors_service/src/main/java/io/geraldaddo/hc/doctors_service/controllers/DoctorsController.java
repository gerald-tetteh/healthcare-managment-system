package io.geraldaddo.hc.doctors_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doctors")
public class DoctorsController {

    @GetMapping
    public String helloWorld() {
        return "Hello World";
    }
}
