package io.geraldaddo.hc.appointments_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.appointments_service.configurations.AppointmentsTestConfiguration;
import io.geraldaddo.hc.appointments_service.dto.*;
import io.geraldaddo.hc.appointments_service.services.AppointmentsService;
import io.geraldaddo.hc.security_module.exception_handlers.AuthExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentsController.class)
@Import({AuthExceptionHandler.class, AppointmentsTestConfiguration.class})
class AppointmentsControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    AppointmentsService appointmentsService;
    @MockitoBean
    KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Authentication patientAuthentication = new UsernamePasswordAuthenticationToken(
            1,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
    );
    private final Authentication doctorAuthentication = new UsernamePasswordAuthenticationToken(
            2,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
    );
    private final Authentication wrongDoctorIdAuthentication = new UsernamePasswordAuthenticationToken(
            1,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
    );
    private final Authentication wrongRoleAuthentication = new UsernamePasswordAuthenticationToken(
            0,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_FALSE"))
    );

    @Test
    void shouldSucceedIfUserInAppointmentIsPrincipal() throws Exception {
        CreateAppointmentDto input = CreateAppointmentDto.builder()
                .doctorId(0)
                .patientId(1)
                .dateTime(LocalDateTime.now())
                .notes("test notes")
                .build();
        when(appointmentsService.createAppointment(any(CreateAppointmentDto.class), anyString()))
                .thenReturn(new AppointmentDto());
        String json = mapper.writeValueAsString(input);
        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer token")
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
                .doctorId(0)
                .patientId(2)
                .dateTime(LocalDateTime.now())
                .notes("test notes")
                .build();
        when(appointmentsService.createAppointment(any(CreateAppointmentDto.class), anyString()))
                .thenReturn(new AppointmentDto());
        String json = mapper.writeValueAsString(input);
        mockMvc.perform(post("/appointments")
                        .header("Authorization", "Bearer token")
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
                .doctorId(1)
                .patientId(0)
                .dateTime(LocalDateTime.now())
                .notes("test notes")
                .build();
        when(appointmentsService.createAppointment(any(CreateAppointmentDto.class), anyString()))
                .thenReturn(new AppointmentDto());
        String json = mapper.writeValueAsString(input);
        // should match on doctor id but role is different
        mockMvc.perform(post("/appointments")
                        .header("Authorization", "Bearer token")
                        .content(json)
                        .with(csrf())
                        .with(authentication(wrongRoleAuthentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldGetDoctorsAppointments() throws Exception {
        when(appointmentsService.getDoctorAppointments(2,0, 10))
                .thenReturn(new PaginatedAppointmentListDto());

        mockMvc.perform(get("/appointments/doctor/2")
                .with(authentication(doctorAuthentication)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(appointmentsService, times(1))
                .getDoctorAppointments(2,0, 10);
    }

    @Test
    void shouldFailToGetDoctorsAppointmentsWithWrongAuthentication() throws Exception {
        // should fail due to wrong role
        mockMvc.perform(get("/appointments/doctor/1")
                        .with(authentication(wrongRoleAuthentication)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        // should fail due to different id
        mockMvc.perform(get("/appointments/doctor/2")
                        .with(authentication(wrongDoctorIdAuthentication)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldGetPatientsAppointments() throws Exception {
        when(appointmentsService.getPatientAppointments(1,0, 10))
                .thenReturn(new PaginatedAppointmentListDto());

        mockMvc.perform(get("/appointments/patient/1")
                        .with(authentication(patientAuthentication)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(appointmentsService, times(1))
                .getPatientAppointments(1,0, 10);
    }

    @Test
    void shouldFailToGetPatientsAppointmentsWithWrongAuthentication() throws Exception {
        // should fail due to invalid role
        mockMvc.perform(get("/appointments/patient/1")
                        .with(authentication(wrongRoleAuthentication)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        // should fail due to different id
        mockMvc.perform(get("/appointments/patient/2")
                        .with(authentication(patientAuthentication)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldApproveDoctorsAppointments() throws Exception {
        AppointmentIdsDto dto = new AppointmentIdsDto(List.of(1,2,3));
        when(appointmentsService.approveAppointments(dto, doctorAuthentication))
                .thenReturn(new AppointmentListDto(List.of()));

        mockMvc.perform(post("/appointments/approve")
                        .with(authentication(doctorAuthentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(appointmentsService, times(1))
                .approveAppointments(dto, doctorAuthentication);
    }

    @Test
    void shouldFailToApproveAppointmentsDueToWrongRole() throws Exception {
        AppointmentIdsDto dto = new AppointmentIdsDto(List.of(1,2,3));

        mockMvc.perform(post("/appointments/approve")
                        .with(authentication(patientAuthentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldCancelAppointment() throws Exception {
        AppointmentIdsDto dto = new AppointmentIdsDto(List.of(1,2,3));
        when(appointmentsService.cancelAppointments(dto, patientAuthentication))
                .thenReturn(new AppointmentListDto(List.of()));

        mockMvc.perform(post("/appointments/cancel")
                        .with(authentication(patientAuthentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(appointmentsService, times(1))
                .cancelAppointments(dto, patientAuthentication);
    }

    @Test
    void shouldFailToCancelAppointmentsDueToWrongRole() throws Exception {
        AppointmentIdsDto dto = new AppointmentIdsDto(List.of(1,2,3));

        mockMvc.perform(post("/appointments/cancel")
                        .with(authentication(wrongRoleAuthentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldRescheduleAppointment() throws Exception {
        LocalDateTime initial = LocalDateTime.now();
        LocalDateTime finalDate = initial.plusHours(1);
        AppointmentDto dto = AppointmentDto.builder()
                .appointmentId(0)
                .doctorId(2)
                .dateTime(initial)
                .build();

        when(appointmentsService.rescheduleAppointment(
                0,finalDate, doctorAuthentication))
                .thenReturn(dto);

        mockMvc.perform(patch("/appointments/0/reschedule")
                        .with(authentication(doctorAuthentication))
                        .queryParam("date", finalDate.toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(appointmentsService, times(1))
                .rescheduleAppointment(0, finalDate, doctorAuthentication);
    }

    @Test
    void shouldFailToRescheduleWithWrongRole() throws Exception {
        LocalDateTime finalDate = LocalDateTime.now();

        mockMvc.perform(patch("/appointments/0/reschedule")
                        .with(authentication(wrongRoleAuthentication))
                        .queryParam("date", finalDate.toString())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(appointmentsService, times(0))
                .rescheduleAppointment(anyInt(), any(), any());
    }
}