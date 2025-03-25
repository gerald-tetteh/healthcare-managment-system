package io.geraldaddo.hc.gateway.services;

import io.geraldaddo.hc.gateway.dtos.LoginDto;
import io.geraldaddo.hc.gateway.dtos.RegisterDto;
import io.geraldaddo.hc.gateway.entities.User;
import io.geraldaddo.hc.gateway.enums.Role;
import io.geraldaddo.hc.gateway.repositories.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientAuthenticationService {
    private final Logger logger = LogManager.getLogger(PatientAuthenticationService.class);
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public PatientAuthenticationService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public User signUp(RegisterDto registerDto) {
        User user = new User()
                .setFirstName(registerDto.firstName())
                .setLastName(registerDto.lastName())
                .setAge(registerDto.age())
                .setDateOfBirth(registerDto.dateOfBirth())
                .setNumber(registerDto.number())
                .setActive(true)
                .setRoles(List.of(Role.PATIENT))
                .setEmergencyFirstName(registerDto.emergencyFirstName())
                .setEmergencyLastName(registerDto.emergencyLastName())
                .setEmergencyNumber(registerDto.emergencyNumber())
                .setPassword(passwordEncoder.encode(registerDto.password()))
                .setEmail(registerDto.email())
                .setAddressLineOne(registerDto.addressLineOne())
                .setAddressLineTwo(registerDto.addressLineTwo())
                .setCountry(registerDto.country())
                .setCounty(registerDto.county())
                .setCity(registerDto.city())
                .setPostCode(registerDto.postCode())
                .setJoined(registerDto.joined());
        return userRepository.save(user);
    }

    public User authenticate(LoginDto loginDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
        );
        return userRepository.findByEmail(loginDto.email()).orElseThrow();
    }
}
