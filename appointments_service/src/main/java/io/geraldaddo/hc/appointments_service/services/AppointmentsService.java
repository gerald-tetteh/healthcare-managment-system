package io.geraldaddo.hc.appointments_service.services;

import io.geraldaddo.hc.appointments_service.dto.CreateAppointmentDto;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
import io.geraldaddo.hc.appointments_service.repositories.AppointmentsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentsService {
    Logger logger = LogManager.getLogger(AppointmentsService.class);
    @Autowired
    AppointmentsRepository appointmentsRepository;

    public Appointment createAppointment(CreateAppointmentDto createAppointmentDto) {
        List<Appointment> existingAppointments = appointmentsRepository
                .countClashingAppointments(
                        createAppointmentDto.getDoctorId(),
                        createAppointmentDto.getPatientId(),
                        createAppointmentDto.getStartDateTime(),
                        createAppointmentDto.getEndDateTime());
        if(!existingAppointments.isEmpty()) {
            IllegalArgumentException ex = new IllegalArgumentException("Appointment clashes with other appointments");
            logger.error("New appointment classes with other exceptions", ex);
            throw ex;
        }
        if(createAppointmentDto.getEndDateTime().isBefore(createAppointmentDto.getStartDateTime())) {
            IllegalArgumentException ex = new IllegalArgumentException("Appointment end date is before start date");
            logger.error("New appointment start date is before end date", ex);
            throw ex;
        }
        Appointment appointment = Appointment.builder()
                .doctorId(createAppointmentDto.getDoctorId())
                .patientId(createAppointmentDto.getPatientId())
                .startDateTime(createAppointmentDto.getStartDateTime())
                .endDateTime(createAppointmentDto.getEndDateTime())
                .build();
        return appointmentsRepository.save(appointment);
    }
}
