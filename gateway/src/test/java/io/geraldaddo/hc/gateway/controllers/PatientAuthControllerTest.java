package io.geraldaddo.hc.gateway.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.gateway.configurations.TestSecurityConfiguration;
import io.geraldaddo.hc.gateway.dtos.LoginDto;
import io.geraldaddo.hc.gateway.dtos.RegisterDto;
import io.geraldaddo.hc.gateway.entities.User;
import io.geraldaddo.hc.gateway.services.JwtService;
import io.geraldaddo.hc.gateway.services.PatientAuthenticationService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientAuthController.class)
@Import({TestSecurityConfiguration.class})
class PatientAuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    PatientAuthenticationService patientAuthenticationService;
    @MockitoBean
    JwtService jwtService;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        RegisterDto registerDto = new RegisterDto(
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
                "Southampton",
                "SO15 2EG",
                "United Kingdom",
                LocalDateTime.parse("2022-03-15T02:30:25")
        );
        mockMvc.perform(post("/auth/patient/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk());
        verify(patientAuthenticationService, times(1)).signUp(registerDto);
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginDto loginDto = new LoginDto("test@example.com","examplePassword");
        when(patientAuthenticationService.authenticate(loginDto)).thenReturn(
                new User()
                        .setEmail(loginDto.email())
                        .setPassword(loginDto.password())
        );
        when(jwtService.buildToken(anyMap(),any(UserDetails.class))).thenReturn("sampleToken4Test");

        mockMvc.perform(post("/auth/patient/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").value("sampleToken4Test"));

        verify(patientAuthenticationService, times(1)).authenticate(loginDto);
        verify(jwtService, times(1)).buildToken(anyMap(),any(UserDetails.class));
    }

    @Test
    void shouldReturnErrorMessageForFailedLogin() throws Exception {
        LoginDto loginDto = new LoginDto("test@example.com","examplePassword");
        when(patientAuthenticationService.authenticate(loginDto)).thenThrow(new AuthenticationException("Test authentication failure") {
            @Override
            public String getMessage() {
                return super.getMessage();
            }
        });

        mockMvc.perform(post("/auth/patient/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title").value("Authentication failed"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.statusCode").exists());

        verify(patientAuthenticationService, times(1)).authenticate(loginDto);
        verify(jwtService, times(0)).buildToken(anyMap(),any());
    }
}