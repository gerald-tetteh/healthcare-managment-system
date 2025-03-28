package io.geraldaddo.hc.gateway.services;

import io.geraldaddo.hc.gateway.dtos.DoctorRegisterDto;
import io.geraldaddo.hc.gateway.dtos.LoginDto;
import io.geraldaddo.hc.gateway.dtos.PatientRegisterDto;
import io.geraldaddo.hc.user_data_module.entities.DoctorProfile;
import io.geraldaddo.hc.user_data_module.entities.User;
import io.geraldaddo.hc.user_data_module.enums.Role;
import io.geraldaddo.hc.user_data_module.repositories.DoctorProfileRepository;
import io.geraldaddo.hc.user_data_module.repositories.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationService {
    private final Logger logger = LogManager.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(
            UserRepository userRepository,
            DoctorProfileRepository doctorProfileRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public User patientSignUp(PatientRegisterDto patientRegisterDto) {
        User user = new User()
                .setFirstName(patientRegisterDto.firstName())
                .setLastName(patientRegisterDto.lastName())
                .setAge(patientRegisterDto.age())
                .setDateOfBirth(patientRegisterDto.dateOfBirth())
                .setNumber(patientRegisterDto.number())
                .setActive(true)
                .setRoles(List.of(Role.PATIENT))
                .setInsuranceNumber(patientRegisterDto.insuranceNumber())
                .setEmergencyFirstName(patientRegisterDto.emergencyFirstName())
                .setEmergencyLastName(patientRegisterDto.emergencyLastName())
                .setEmergencyNumber(patientRegisterDto.emergencyNumber())
                .setPassword(passwordEncoder.encode(patientRegisterDto.password()))
                .setEmail(patientRegisterDto.email())
                .setAddressLineOne(patientRegisterDto.addressLineOne())
                .setAddressLineTwo(patientRegisterDto.addressLineTwo())
                .setCountry(patientRegisterDto.country())
                .setCounty(patientRegisterDto.county())
                .setCity(patientRegisterDto.city())
                .setPostCode(patientRegisterDto.postCode())
                .setJoined(patientRegisterDto.joined());
        return userRepository.save(user);
    }

    public DoctorProfile doctorSignUp(DoctorRegisterDto doctorRegisterDto) {
        User user = new User()
                .setFirstName(doctorRegisterDto.firstName())
                .setLastName(doctorRegisterDto.lastName())
                .setAge(doctorRegisterDto.age())
                .setDateOfBirth(doctorRegisterDto.dateOfBirth())
                .setNumber(doctorRegisterDto.number())
                .setActive(true)
                .setRoles(List.of(Role.DOCTOR))
                .setInsuranceNumber(doctorRegisterDto.insuranceNumber())
                .setEmergencyFirstName(doctorRegisterDto.emergencyFirstName())
                .setEmergencyLastName(doctorRegisterDto.emergencyLastName())
                .setEmergencyNumber(doctorRegisterDto.emergencyNumber())
                .setPassword(passwordEncoder.encode(doctorRegisterDto.password()))
                .setEmail(doctorRegisterDto.email())
                .setAddressLineOne(doctorRegisterDto.addressLineOne())
                .setAddressLineTwo(doctorRegisterDto.addressLineTwo())
                .setCountry(doctorRegisterDto.country())
                .setCounty(doctorRegisterDto.county())
                .setCity(doctorRegisterDto.city())
                .setPostCode(doctorRegisterDto.postCode())
                .setJoined(doctorRegisterDto.joined());
        User savedUser = userRepository.save(user);
        DoctorProfile profile = new DoctorProfile()
                .setAvailabilityList(doctorRegisterDto.availabilityList())
                .setConsultationFee(doctorRegisterDto.consultationFee())
                .setLicenseNumber(doctorRegisterDto.licenseNumber())
                .setSpecialisation(doctorRegisterDto.specialisation())
                .setUserProfile(savedUser);
        return doctorProfileRepository.save(profile);
    }

    public User authenticate(LoginDto loginDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
        );
        return userRepository.findByEmail(loginDto.email()).orElseThrow();
    }
}
