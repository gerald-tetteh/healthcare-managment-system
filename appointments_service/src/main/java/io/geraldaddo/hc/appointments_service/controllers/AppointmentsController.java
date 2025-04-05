package io.geraldaddo.hc.appointments_service.controllers;

import io.geraldaddo.hc.appointments_service.dto.AppointmentDto;
import io.geraldaddo.hc.appointments_service.dto.CreateAppointmentDto;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
import io.geraldaddo.hc.appointments_service.services.AppointmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
public class AppointmentsController {
    @Autowired
    private AppointmentsService appointmentsService;

    @PostMapping
    @PreAuthorize("(authentication.principal == #createAppointmentDto.patientId && hasRole('PATIENT')) " +
            "|| (authentication.principal == #createAppointmentDto.doctorId && hasRole('DOCTOR')) " +
            "|| hasRole('ADMIN')")
    public ResponseEntity<AppointmentDto> createAppointment(@RequestBody CreateAppointmentDto createAppointmentDto) {
        Appointment appointment = appointmentsService.createAppointment(createAppointmentDto);
        return ResponseEntity.ok(AppointmentDto.builder()
                .appointmentId(appointment.getAppointmentId())
                .doctorId(appointment.getDoctorId())
                .patientId(appointment.getPatientId())
                .startDateTime(appointment.getStartDateTime())
                .endDateTime(appointment.getEndDateTime()).build());
    }
}
