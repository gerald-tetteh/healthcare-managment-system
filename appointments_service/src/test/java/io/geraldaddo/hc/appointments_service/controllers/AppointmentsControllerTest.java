package io.geraldaddo.hc.appointments_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.appointments_service.configurations.AppointmentsTestConfiguration;
import io.geraldaddo.hc.appointments_service.dto.AppointmentDto;
import io.geraldaddo.hc.appointments_service.dto.CreateAppointmentDto;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
import io.geraldaddo.hc.appointments_service.services.AppointmentsService;
import io.geraldaddo.hc.security_module.exception_handlers.AuthExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentsController.class)
@Import({AuthExceptionHandler.class, AppointmentsTestConfiguration.class})
class AppointmentsControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    AppointmentsService appointmentsService;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Authentication patientAuthentication = new UsernamePasswordAuthenticationToken(
            1,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
    );
    private final Authentication wrongRoleAuthentication = new UsernamePasswordAuthenticationToken(
            0,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_FALSE"))
    );

    @Test
    void shouldSucceedIfUserInAppointmentIsPrincipal() throws Exception {
        CreateAppointmentDto input = CreateAppointmentDto.builder()
                .patientId(1)
                .doctorId(0)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .build();
        when(appointmentsService.createAppointment(any(CreateAppointmentDto.class)))
                .thenReturn(new Appointment());
        String json = mapper.writeValueAsString(input);
        mockMvc.perform(post("/appointments")
                .content(json)
                .with(authentication(patientAuthentication))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(new AppointmentDto())));
    }

    @Test
    void shouldFailIfPatientInAppointmentIsNotPrincipal() throws Exception {
        CreateAppointmentDto input = CreateAppointmentDto.builder()
                .patientId(2)
                .doctorId(0)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .build();
        when(appointmentsService.createAppointment(any(CreateAppointmentDto.class)))
                .thenReturn(new Appointment());
        String json = mapper.writeValueAsString(input);
        mockMvc.perform(post("/appointments")
                        .content(json)
                        .with(csrf())
                        .with(authentication(patientAuthentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldFailIfUserHasValidIdButWrongRole() throws Exception {
        CreateAppointmentDto input = CreateAppointmentDto.builder()
                .patientId(1)
                .doctorId(0)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .build();
        when(appointmentsService.createAppointment(any(CreateAppointmentDto.class)))
                .thenReturn(new Appointment());
        String json = mapper.writeValueAsString(input);
        // should match on doctor id but role is different
        mockMvc.perform(post("/appointments")
                        .content(json)
                        .with(csrf())
                        .with(authentication(wrongRoleAuthentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}