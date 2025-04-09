package io.geraldaddo.hc.appointments_service.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.appointments_service.dto.AppointmentDto;
import io.geraldaddo.hc.appointments_service.dto.AppointmentListDto;
import io.geraldaddo.hc.appointments_service.dto.CreateAppointmentDto;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
import io.geraldaddo.hc.appointments_service.services.AppointmentsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
        Appointment appointment = appointmentsService.createAppointment(createAppointmentDto, token);
        logger.info(String.format("appointment created between doctor: %d and patient: %d at %s",
                appointment.getDoctorId(), appointment.getPatientId(), appointment.getDateTime()));
        sendKafkaMessage(appointment);
        logger.info(String.format("sent kafka message for appointment: %d", appointment.getAppointmentId()));
        return ResponseEntity.ok(buildAppointmentDto(appointment));
    }

    @GetMapping("/doctor/{id}")
    @PreAuthorize("(authentication.principal == #id && hasRole('DOCTOR')) || hasRole('ADMIN')")
    public ResponseEntity<AppointmentListDto> getDoctorAppointments(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10", name = "size") int numberOfRecords) {
        List<Appointment> appointments = appointmentsService.getDoctorAppointments(id,page,numberOfRecords);
        List<AppointmentDto> appointmentDtos = appointments.stream().map(this::buildAppointmentDto)
                .toList();
        return ResponseEntity.ok(AppointmentListDto.builder()
                .appointments(appointmentDtos)
                .page(0)
                .numberOfRecords(appointmentDtos.size())
                .build());
    }

    @GetMapping("/patient/{id}")
    @PreAuthorize("(authentication.principal == #id && hasRole('PATIENT')) || hasRole('ADMIN')")
    public ResponseEntity<AppointmentListDto> getPatientAppointments(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10", name = "size") int numberOfRecords) {
        List<Appointment> appointments = appointmentsService.getPatientAppointments(id,page,numberOfRecords);
        List<AppointmentDto> appointmentDtos = appointments.stream().map(this::buildAppointmentDto)
                .toList();
        return ResponseEntity.ok(AppointmentListDto.builder()
                .appointments(appointmentDtos)
                .page(0)
                .numberOfRecords(appointmentDtos.size())
                .build());
    }

    private void sendKafkaMessage(Object object) throws JsonProcessingException {
        String message = mapper.writeValueAsString(object);
        kafkaTemplate.send(kafkaTopic, message);
    }
    private AppointmentDto buildAppointmentDto(Appointment appointment) {
        return AppointmentDto.builder()
                .appointmentId(appointment.getAppointmentId())
                .doctorId(appointment.getDoctorId())
                .patientId(appointment.getPatientId())
                .status(appointment.getStatus())
                .dateTime(appointment.getDateTime())
                .notes(appointment.getNotes())
                .build();
    }
}
