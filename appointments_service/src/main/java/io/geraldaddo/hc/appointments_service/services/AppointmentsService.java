package io.geraldaddo.hc.appointments_service.services;

import io.geraldaddo.hc.appointments_service.dto.*;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
import io.geraldaddo.hc.appointments_service.entities.AppointmentStatus;
import io.geraldaddo.hc.appointments_service.exceptions.AppointmentsServerException;
import io.geraldaddo.hc.appointments_service.repositories.AppointmentsRepository;
import io.geraldaddo.hc.cache_module.utils.CacheUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

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

    public AppointmentDto createAppointment(CreateAppointmentDto createAppointmentDto, String token) {
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
        return buildAppointmentDto(savedAppointment);
    }

    @Cacheable(
            value = "appointments", key = "#doctorId + '_' + #page + '_' + #numberOfRecords",
            unless = "#result.numberOfRecords < 1")
    public PaginatedAppointmentListDto getDoctorAppointments(int doctorId, int page, int numberOfRecords) {
        Pageable pageable = PageRequest.of(page, numberOfRecords);
        Page<Appointment> slice = appointmentsRepository.findAllByDoctorId(doctorId, pageable);
        return PaginatedAppointmentListDto
                .builder()
                .appointments(slice.getContent().stream().map(this::buildAppointmentDto).toList())
                .numberOfRecords(slice.getNumberOfElements())
                .totalAppointments(slice.getTotalElements())
                .page(page)
                .build();
    }

    @Cacheable(value = "appointments", key = "#patientId + '_' + #page + '_' + #numberOfRecords",
            unless = "#result.numberOfRecords < 1")
    public PaginatedAppointmentListDto getPatientAppointments(int patientId, int page, int numberOfRecords) {
        Pageable pageable = PageRequest.of(page, numberOfRecords);
        Page<Appointment> slice = appointmentsRepository.findAllByPatientId(patientId, pageable);
        return PaginatedAppointmentListDto
                .builder()
                .appointments(slice.getContent().stream().map(this::buildAppointmentDto).toList())
                .numberOfRecords(slice.getNumberOfElements())
                .totalAppointments(slice.getTotalElements())
                .page(page)
                .build();
    }

    public AppointmentListDto approveAppointments(AppointmentIdsDto appointmentIdsDto, Authentication authentication) {
        Stream<Appointment> appointmentStream = setAppointmentsToScheduled(appointmentIdsDto, authentication);
        List<AppointmentDto> dtos = appointmentsRepository
                .saveAll(appointmentStream.toList())
                .stream()
                .map(this::buildAppointmentDto)
                .toList();
        deleteCachedAppointments(dtos);
        return new AppointmentListDto(dtos);
    }

    public AppointmentListDto cancelAppointments(AppointmentIdsDto appointmentIdsDto, Authentication authentication) {
        Stream<Appointment> appointmentStream = setAppointmentsToCanceled(appointmentIdsDto, authentication);
        List<AppointmentDto> dtos = appointmentsRepository
                .saveAll(appointmentStream.toList())
                .stream()
                .map(this::buildAppointmentDto)
                .toList();
        deleteCachedAppointments(dtos);
        return new AppointmentListDto(dtos);
    }

    public AppointmentDto rescheduleAppointment(
            int appointmentId, LocalDateTime dateTime, Authentication authentication) {
        Appointment appointment = getAppointmentById(appointmentId);
        GrantedAuthority authority = authentication.getAuthorities().stream().findFirst()
                .orElseThrow(() -> new AuthorizationDeniedException("Not authorised to perform action"));
        int userId = (int) authentication.getPrincipal();
        if((appointment.getPatientId() == userId && authority.getAuthority().equals("ROLE_PATIENT"))
                || (appointment.getDoctorId() == userId && authority.getAuthority().equals("ROLE_DOCTOR"))) {
            appointment.setDateTime(dateTime);
            Appointment savedAppointment = appointmentsRepository.save(appointment);
            cacheUtils.evictFromCacheByKeyMatch(
                    "appointments", savedAppointment.getDoctorId().toString());
            cacheUtils.evictFromCacheByKeyMatch(
                    "appointments", savedAppointment.getPatientId().toString());
            return this.buildAppointmentDto(savedAppointment);
        }
        throw new AuthorizationDeniedException("Not authorised to perform action");
    }

    protected void deleteCachedAppointments(List<AppointmentDto> dtos) {
        dtos.stream()
                .flatMap(ap -> Stream.of(ap.getDoctorId(), ap.getDoctorId()))
                .distinct()
                .forEach(id -> cacheUtils.evictFromCacheByKeyMatch("appointments", String.valueOf(id)));
    }
    protected Appointment getAppointmentById(int appointmentId) {
        return appointmentsRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Appointment: %s does not exist", appointmentId)));
    }
    protected Stream<Appointment> setAppointmentsToScheduled(
            AppointmentIdsDto appointmentIdsDto, Authentication authentication) {
        return appointmentsRepository.findAllById(appointmentIdsDto.ids()).stream()
                .filter(ap -> ap.getDoctorId() == authentication.getPrincipal()
                                && ap.getStatus() == AppointmentStatus.PENDING)
                .peek(ap -> ap.setStatus(AppointmentStatus.SCHEDULED));
    }
    protected Stream<Appointment> setAppointmentsToCanceled(
            AppointmentIdsDto appointmentIdsDto, Authentication authentication) {
        return appointmentsRepository.findAllById(appointmentIdsDto.ids()).stream()
                .filter(ap -> (ap.getDoctorId() == authentication.getPrincipal()
                        || ap.getPatientId() == authentication.getPrincipal())
                        && ap.getStatus() != AppointmentStatus.CANCELED)
                .peek(ap -> ap.setStatus(AppointmentStatus.CANCELED));
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
