package io.geraldaddo.hc.appointments_service.services;

import io.geraldaddo.hc.appointments_service.dto.CreateAppointmentDto;
import io.geraldaddo.hc.appointments_service.dto.DoctorAvailableDto;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
import io.geraldaddo.hc.appointments_service.entities.AppointmentStatus;
import io.geraldaddo.hc.appointments_service.exceptions.AppointmentsServerException;
import io.geraldaddo.hc.appointments_service.repositories.AppointmentsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AppointmentsService {
    private final Logger logger = LogManager.getLogger(AppointmentsService.class);
    private final AppointmentsRepository appointmentsRepository;
    private final WebClient doctorsWebClient;

    public AppointmentsService(
            AppointmentsRepository appointmentsRepository,
            @Qualifier(value = "doctors-service") WebClient doctorsWebClient) {
        this.appointmentsRepository = appointmentsRepository;
        this.doctorsWebClient = doctorsWebClient;
    }

    public Appointment createAppointment(CreateAppointmentDto createAppointmentDto, String token) {
        DoctorAvailableDto availableDto = doctorsWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/{doctorId}/available/{date}")
                        .build(createAppointmentDto.getDoctorId(), createAppointmentDto.getDateTime()))
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(DoctorAvailableDto.class)
                .block();
        if(availableDto == null) {
            RuntimeException ex = new AppointmentsServerException("Could not validate doctors availability");
            logger.error("Could not get doctors availability", ex);
            throw ex;
        }
        if(!availableDto.isAvailable()) {
            RuntimeException ex = new IllegalArgumentException("Doctor is unavailable at specified time");
            logger.error("Doctor is not available at specified time", ex);
            throw ex;
        }
        Appointment appointment = Appointment.builder()
                .doctorId(createAppointmentDto.getDoctorId())
                .patientId(createAppointmentDto.getPatientId())
                .dateTime(createAppointmentDto.getDateTime())
                .notes(createAppointmentDto.getNotes())
                .status(AppointmentStatus.PENDING)
                .build();
        return appointmentsRepository.save(appointment);
    }
}
