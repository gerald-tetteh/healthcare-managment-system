package io.geraldaddo.hc.appointments_service.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.appointments_service.dto.*;
import io.geraldaddo.hc.appointments_service.services.AppointmentsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/appointments")
public class AppointmentsController {
    private final Logger logger = LogManager.getLogger(AppointmentsController.class);
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Autowired
    private AppointmentsService appointmentsService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Value("${kafka.topic}")
    private String kafkaTopic;

    @PostMapping
    @PreAuthorize("(authentication.principal == #createAppointmentDto.patientId && hasRole('PATIENT')) " +
            "|| (authentication.principal == #createAppointmentDto.doctorId && hasRole('DOCTOR')) " +
            "|| hasRole('ADMIN')")
    public ResponseEntity<AppointmentDto> createAppointment(
            @RequestBody CreateAppointmentDto createAppointmentDto,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws JsonProcessingException {
        AppointmentDto appointment = appointmentsService.createAppointment(createAppointmentDto, token);
        logger.info(String.format("appointment created between doctor: %d and patient: %d at %s",
                appointment.getDoctorId(), appointment.getPatientId(), appointment.getDateTime()));
        sendKafkaMessage(appointment);
        logger.info(String.format("sent kafka message for appointment: %d", appointment.getAppointmentId()));
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/doctor/{id}")
    @PreAuthorize("(authentication.principal == #id && hasRole('DOCTOR')) || hasRole('ADMIN')")
    public ResponseEntity<PaginatedAppointmentListDto> getDoctorAppointments(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10", name = "size") int numberOfRecords) {
        return ResponseEntity.ok(appointmentsService.getDoctorAppointments(id,page,numberOfRecords));
    }

    @GetMapping("/patient/{id}")
    @PreAuthorize("(authentication.principal == #id && hasRole('PATIENT')) || hasRole('ADMIN')")
    public ResponseEntity<PaginatedAppointmentListDto> getPatientAppointments(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10", name = "size") int numberOfRecords) {
        return ResponseEntity.ok(appointmentsService.getPatientAppointments(id,page,numberOfRecords));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('DOCTOR') || hasRole('ADMIN')")
    public ResponseEntity<AppointmentListDto> approveAppointment(
            @RequestBody AppointmentIdsDto appointmentIdsDto, Authentication authentication) {
        return ResponseEntity.ok(appointmentsService.approveAppointments(appointmentIdsDto, authentication));
    }

    private void sendKafkaMessage(Object object) throws JsonProcessingException {
        String message = mapper.writeValueAsString(object);
        kafkaTemplate.send(kafkaTopic, message);
    }
}
