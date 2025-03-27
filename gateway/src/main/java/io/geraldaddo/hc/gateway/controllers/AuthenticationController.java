package io.geraldaddo.hc.gateway.controllers;

import io.geraldaddo.hc.gateway.dtos.DoctorRegisterDto;
import io.geraldaddo.hc.gateway.dtos.LoginDto;
import io.geraldaddo.hc.gateway.dtos.LoginResponseDto;
import io.geraldaddo.hc.gateway.dtos.PatientRegisterDto;
import io.geraldaddo.hc.gateway.services.AuthenticationService;
import io.geraldaddo.hc.gateway.services.JwtService;
import io.geraldaddo.hc.user_data_module.entities.DoctorProfile;
import io.geraldaddo.hc.user_data_module.entities.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final Logger logger = LogManager.getLogger(AuthenticationController.class);
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/patient/register")
    public ResponseEntity<HttpStatus> register(@RequestBody PatientRegisterDto patientRegisterDto) {
        User user = authenticationService.patientSignUp(patientRegisterDto);
        logger.info(String.format("created new patient record: %d", user.getUserId()));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/doctor/register")
    public ResponseEntity<HttpStatus> register(@RequestBody DoctorRegisterDto doctorRegisterDto) {
        DoctorProfile profile = authenticationService.doctorSignUp(doctorRegisterDto);
        logger.info(String.format("created new doctor record: %d", profile.getUserProfile().getUserId()));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto) {
        User user = authenticationService.authenticate(loginDto);
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("userId", user.getUserId());
        claims.put("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        String token = jwtService.buildToken(claims, user);
        LoginResponseDto loginResponseDto = new LoginResponseDto(token);
        logger.info(String.format("user %d logged in", user.getUserId()));
        return ResponseEntity.ok(loginResponseDto);
    }
}
