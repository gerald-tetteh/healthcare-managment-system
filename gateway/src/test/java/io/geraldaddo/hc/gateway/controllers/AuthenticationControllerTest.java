package io.geraldaddo.hc.gateway.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.gateway.configurations.TestSecurityConfiguration;
import io.geraldaddo.hc.gateway.dtos.DoctorRegisterDto;
import io.geraldaddo.hc.gateway.dtos.LoginDto;
import io.geraldaddo.hc.gateway.dtos.PatientRegisterDto;
import io.geraldaddo.hc.gateway.services.AuthenticationService;
import io.geraldaddo.hc.gateway.services.JwtService;
import io.geraldaddo.hc.user_data_module.entities.Availability;
import io.geraldaddo.hc.user_data_module.entities.DoctorProfile;
import io.geraldaddo.hc.user_data_module.entities.User;
import io.geraldaddo.hc.user_data_module.enums.DayOfWeek;
import io.geraldaddo.hc.user_data_module.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import({TestSecurityConfiguration.class})
class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    AuthenticationService authenticationService;
    @MockitoBean
    JwtService jwtService;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldRegisterPatientSuccessfully() throws Exception {
        PatientRegisterDto patientRegisterDto = new PatientRegisterDto(
                "Gerald",
                "Addo-Tetteh",
                24,
                LocalDate.parse("2001-01-24"),
                "+447350802170",
                "James",
                "Addo-Tetteh",
                "+233208781883",
                "simple4Password&done",
                "geraldadt@outlook.com",
                "Flat 12 Andromeda House",
                "18 Southampton Street",
                "Hampshire",
                "282-283-284",
                "Southampton",
                "SO15 2EG",
                "United Kingdom",
                LocalDateTime.parse("2022-03-15T02:30:25")
        );
        when(authenticationService.patientSignUp(patientRegisterDto))
                .thenReturn(new User().setUserId(0));
        mockMvc.perform(post("/auth/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(patientRegisterDto)))
                .andExpect(status().isOk());
        verify(authenticationService, times(1)).patientSignUp(patientRegisterDto);
    }

    @Test
    void shouldRegisterDoctorSuccessfully() throws Exception {
        DoctorRegisterDto doctorRegisterDto = new DoctorRegisterDto(
                "Gerald",
                "Addo-Tetteh",
                24,
                LocalDate.parse("2001-01-24"),
                "+447350802170",
                "James",
                "Addo-Tetteh",
                "+233208781883",
                "simple4Password&done",
                "geraldadt@outlook.com",
                "Flat 12 Andromeda House",
                "18 Southampton Street",
                "Hampshire",
                "282-283-284",
                "Southampton",
                "SO15 2EG",
                "United Kingdom",
                LocalDateTime.parse("2022-03-15T02:30:25"),
                "343-354-839",
                "Dermatology",
                145.0,
                List.of(
                        new Availability(
                                LocalTime.parse("09:00:00"), LocalTime.parse("17:00:00"), DayOfWeek.MONDAY
                        )
                )
        );
        when(authenticationService.doctorSignUp(doctorRegisterDto))
                .thenReturn(new DoctorProfile()
                        .setUserProfile(new User().setUserId(0)));
        mockMvc.perform(post("/auth/doctor/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(doctorRegisterDto)))
                .andExpect(status().isOk());
        verify(authenticationService, times(1)).doctorSignUp(doctorRegisterDto);
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginDto loginDto = new LoginDto("test@example.com","examplePassword");
        when(authenticationService.authenticate(loginDto)).thenReturn(
                new User()
                        .setEmail(loginDto.email())
                        .setPassword(loginDto.password())
                        .setRoles(List.of(Role.PATIENT))
        );
        when(jwtService.buildToken(anyMap(),any(UserDetails.class))).thenReturn("sampleToken4Test");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").value("sampleToken4Test"));

        verify(authenticationService, times(1)).authenticate(loginDto);
        verify(jwtService, times(1)).buildToken(anyMap(),any(UserDetails.class));
    }

    @Test
    void shouldReturnErrorMessageForFailedLogin() throws Exception {
        LoginDto loginDto = new LoginDto("test@example.com","examplePassword");
        when(authenticationService.authenticate(loginDto)).thenThrow(new AuthenticationException("Test authentication failure") {
            @Override
            public String getMessage() {
                return super.getMessage();
            }
        });

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title").value("Authentication failed"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.statusCode").exists());

        verify(authenticationService, times(1)).authenticate(loginDto);
        verify(jwtService, times(0)).buildToken(anyMap(),any());
    }
}