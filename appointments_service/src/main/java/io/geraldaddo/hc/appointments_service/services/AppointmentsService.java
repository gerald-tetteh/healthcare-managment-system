package io.geraldaddo.hc.appointments_service.services;

import io.geraldaddo.hc.appointments_service.dto.CreateAppointmentDto;
import io.geraldaddo.hc.appointments_service.dto.DoctorAvailableDto;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
import io.geraldaddo.hc.appointments_service.entities.AppointmentStatus;
import io.geraldaddo.hc.appointments_service.exceptions.AppointmentsServerException;
import io.geraldaddo.hc.appointments_service.repositories.AppointmentsRepository;
import io.geraldaddo.hc.cache_module.utils.CacheUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class AppointmentsService {
    private final Logger logger = LogManager.getLogger(AppointmentsService.class);
    private final AppointmentsRepository appointmentsRepository;
    private final CacheUtils cacheUtils;
    private final WebClient doctorsWebClient;

    public AppointmentsService(
            AppointmentsRepository appointmentsRepository,
            CacheUtils cacheUtils,
            @Qualifier(value = "doctors-service") WebClient doctorsWebClient) {
        this.appointmentsRepository = appointmentsRepository;
        this.doctorsWebClient = doctorsWebClient;
        this.cacheUtils = cacheUtils;
    }

    public Appointment createAppointment(CreateAppointmentDto createAppointmentDto, String token) {
        DoctorAvailableDto availableDto = doctorsWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/{doctorId}/available/{date}")
                        .build(createAppointmentDto.getDoctorId(), createAppointmentDto.getDateTime()))
                .header(HttpHeaders.AUTHORIZATION, token)
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
        Appointment savedAppointment = appointmentsRepository.save(appointment);
        cacheUtils.evictFromCacheByKeyMatch(
                "appointments", savedAppointment.getDoctorId().toString());
        return savedAppointment;
    }

    @Cacheable(
            value = "appointments", key = "#doctorId + '_' + #page + '_' + #numberOfRecords",
            unless = "#result.isEmpty()")
    public List<Appointment> getDoctorAppointments(int doctorId, int page, int numberOfRecords) {
        Pageable pageable = PageRequest.of(page, numberOfRecords);
        return appointmentsRepository.findAllByDoctorId(doctorId, pageable);
    }

    @Cacheable(value = "appointments", key = "#patientId + '_' + #page + '_' + #numberOfRecords",
            unless = "#result.isEmpty()")
    public List<Appointment> getPatientAppointments(int patientId, int page, int numberOfRecords) {
        Pageable pageable = PageRequest.of(page, numberOfRecords);
        return appointmentsRepository.findAllByPatientId(patientId, pageable);
    }
}
