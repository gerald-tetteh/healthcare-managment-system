package io.geraldaddo.hc.appointments_service.controllers;

import io.geraldaddo.hc.appointments_service.dto.AppointmentDto;
import io.geraldaddo.hc.appointments_service.dto.CreateAppointmentDto;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
import io.geraldaddo.hc.appointments_service.services.AppointmentsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/appointments")
public class AppointmentsController {
    private final Logger logger = LogManager.getLogger(AppointmentsController.class);
    @Autowired
    private AppointmentsService appointmentsService;

    @PostMapping
    @PreAuthorize("(authentication.principal == #createAppointmentDto.patientId && hasRole('PATIENT')) " +
            "|| (authentication.principal == #createAppointmentDto.doctorId && hasRole('DOCTOR')) " +
            "|| hasRole('ADMIN')")
    public ResponseEntity<AppointmentDto> createAppointment(
            @RequestBody CreateAppointmentDto createAppointmentDto,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        Appointment appointment = appointmentsService.createAppointment(createAppointmentDto, token);
        return ResponseEntity.ok(AppointmentDto.builder()
                .appointmentId(appointment.getAppointmentId())
                .doctorId(appointment.getDoctorId())
                .patientId(appointment.getPatientId())
                .status(appointment.getStatus())
                .dateTime(appointment.getDateTime())
                .notes(appointment.getNotes())
                .build());
    }
}
