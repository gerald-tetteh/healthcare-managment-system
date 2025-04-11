package io.geraldaddo.hc.doctors_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.doctors_service.dto.DoctorsAvailabilityDto;
import io.geraldaddo.hc.doctors_service.dto.UpdateDoctorProfileDto;
import io.geraldaddo.hc.doctors_service.services.DoctorsService;
import io.geraldaddo.hc.security_module.exception_handlers.AuthExceptionHandler;
import io.geraldaddo.hc.user_data_module.entities.DoctorProfile;
import io.geraldaddo.hc.user_data_module.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorsController.class)
@Import(AuthExceptionHandler.class)
@EnableMethodSecurity
class DoctorsControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    DoctorsService doctorsService;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        when(doctorsService.getProfile(0)).thenReturn(new DoctorProfile()
                .setUserProfile(new User()));
    }

    void setUserWithWrongId() {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                1,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(token);
    }
    void setAuthorisedUser() {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                0,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    void shouldGetDoctorsProfile() throws Exception {
        setAuthorisedUser();
        mockMvc.perform(get("/doctors/0"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailToGetDoctorsProfile() throws Exception {
        setUserWithWrongId();
        mockMvc.perform(get("/doctors/0"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Authorisation failed"));
    }

    @Test
    void shouldGetAvailability() throws Exception {
        setAuthorisedUser();
        when(doctorsService.getAvailability(anyInt(), any(LocalDateTime.class)))
                .thenReturn(new DoctorsAvailabilityDto());
        mockMvc.perform(get("/doctors/0/availability"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        verify(doctorsService, times(1))
                .getAvailability(anyInt(), any(LocalDateTime.class));
    }

    @Test
    void shouldFailToUpdateProfile() throws Exception {
        setUserWithWrongId();
        UpdateDoctorProfileDto updateDoctorProfileDto = UpdateDoctorProfileDto.builder()
                .specialisation("Radiologist")
                .consultationFee(456.4)
                .licenseNumber("383-284-485")
                .availabilityList(List.of())
                .build();
        String json = mapper.writeValueAsString(updateDoctorProfileDto);
        mockMvc.perform(put("/doctors/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Authorisation failed"));
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        setAuthorisedUser();
        UpdateDoctorProfileDto updateDoctorProfileDto = UpdateDoctorProfileDto.builder()
                .specialisation("Radiologist")
                .consultationFee(456.4)
                .licenseNumber("383-284-485")
                .availabilityList(List.of())
                .build();
        when(doctorsService.updateProfile(anyInt(), any(UpdateDoctorProfileDto.class)))
                .thenReturn(new DoctorProfile().setUserProfile(new User()));
        String json = mapper.writeValueAsString(updateDoctorProfileDto);
        mockMvc.perform(put("/doctors/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(doctorsService, times(1))
                .updateProfile(anyInt(), any(UpdateDoctorProfileDto.class));
    }
}