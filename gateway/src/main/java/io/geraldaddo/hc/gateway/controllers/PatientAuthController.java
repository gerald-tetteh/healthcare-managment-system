package io.geraldaddo.hc.gateway.controllers;

import io.geraldaddo.hc.gateway.dtos.LoginDto;
import io.geraldaddo.hc.gateway.dtos.LoginResponseDto;
import io.geraldaddo.hc.gateway.dtos.RegisterDto;
import io.geraldaddo.hc.gateway.entities.User;
import io.geraldaddo.hc.gateway.services.JwtService;
import io.geraldaddo.hc.gateway.services.PatientAuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/patient")
public class PatientAuthController {
    private final Logger logger = LogManager.getLogger(PatientAuthController.class);
    private final JwtService jwtService;
    private final PatientAuthenticationService patientAuthenticationService;

    public PatientAuthController(JwtService jwtService, PatientAuthenticationService patientAuthenticationService) {
        this.jwtService = jwtService;
        this.patientAuthenticationService = patientAuthenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@RequestBody RegisterDto registerDto) {
        User user = patientAuthenticationService.signUp(registerDto);
        logger.info(String.format("created new patient record: %d", user.getUserId()));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto) {
        User user = patientAuthenticationService.authenticate(loginDto);
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("userId", user.getUserId());
        String token = jwtService.buildToken(claims, user);
        LoginResponseDto loginResponseDto = new LoginResponseDto(token);
        logger.info(String.format("user %d logged in", user.getUserId()));
        return ResponseEntity.ok(loginResponseDto);
    }
}
