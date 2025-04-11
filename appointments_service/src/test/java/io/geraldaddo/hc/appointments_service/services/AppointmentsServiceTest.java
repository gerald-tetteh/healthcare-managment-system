package io.geraldaddo.hc.appointments_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.appointments_service.configurations.AppointmentsTestConfiguration;
import io.geraldaddo.hc.appointments_service.dto.*;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
import io.geraldaddo.hc.appointments_service.entities.AppointmentStatus;
import io.geraldaddo.hc.appointments_service.exceptions.AppointmentsServerException;
import io.geraldaddo.hc.appointments_service.repositories.AppointmentsRepository;
import io.geraldaddo.hc.cache_module.utils.CacheUtils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import({AppointmentsTestConfiguration.class})
class AppointmentsServiceTest {
    private static MockWebServer mockWebServer;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    @MockitoBean
    AppointmentsRepository appointmentsRepository;
    @MockitoBean
    CacheUtils cacheUtils;

    AppointmentsService underTest;

    @BeforeEach
    void beforeEach() {
        WebClient client = WebClient.builder()
                .baseUrl(String.format("http://localhost:%s", mockWebServer.getPort()))
                .build();
        underTest = new AppointmentsService(appointmentsRepository, cacheUtils, client);
    }

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    public void shouldCreateAppointment() throws JsonProcessingException {
        CreateAppointmentDto createAppointmentDto = CreateAppointmentDto.builder()
                .doctorId(0)
                .patientId(0)
                .dateTime(LocalDateTime.now())
                .notes("test notes")
                .build();
        when(appointmentsRepository.save(any(Appointment.class)))
                .thenReturn(Appointment.builder().doctorId(0).build());
        mockWebServer.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(new DoctorAvailableDto(true)))
                .addHeader("Content-Type", "application/json"));

        underTest.createAppointment(createAppointmentDto, "");

        verify(appointmentsRepository, times(1))
                .save(any(Appointment.class));
    }

    @Test
    public void shouldThrowExceptionIfDoctorUnavailable() throws JsonProcessingException {
        CreateAppointmentDto createAppointmentDto = CreateAppointmentDto.builder()
                .doctorId(0)
                .patientId(0)
                .dateTime(LocalDateTime.now())
                .notes("test notes")
                .build();
        mockWebServer.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(new DoctorAvailableDto(false)))
                .addHeader("Content-Type", "application/json"));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                underTest.createAppointment(createAppointmentDto, ""));
        assertEquals("Doctor is unavailable at specified time", exception.getMessage());

        verify(appointmentsRepository, times(0))
                .save(any(Appointment.class));
    }

    @Test
    public void shouldThrowExceptionIfDoctorServiceUnavailable() {
        CreateAppointmentDto createAppointmentDto = CreateAppointmentDto.builder()
                .doctorId(0)
                .patientId(0)
                .dateTime(LocalDateTime.now())
                .notes("test notes")
                .build();
        mockWebServer.enqueue(new MockResponse());

        Exception exception = assertThrows(AppointmentsServerException.class, () ->
                underTest.createAppointment(createAppointmentDto, ""));
        assertEquals("Could not validate doctors availability", exception.getMessage());

        verify(appointmentsRepository, times(0))
                .save(any(Appointment.class));
    }

    @Test
    public void shouldGetDoctorsAppointments() {
        Pageable pageable = PageRequest.of(0, 10);
        when(appointmentsRepository.findAllByDoctorId(0, pageable)).thenReturn(
                new PageImpl<>(List.of(new Appointment()))
        );

        underTest.getDoctorAppointments(0,0,10);

        verify(appointmentsRepository, times(1)).findAllByDoctorId(0, pageable);
    }

    @Test
    public void shouldGetPatientsAppointments() {
        Pageable pageable = PageRequest.of(0, 10);
        when(appointmentsRepository.findAllByPatientId(0, pageable)).thenReturn(
                new PageImpl<>(List.of(new Appointment()))
        );

        underTest.getPatientAppointments(0,0,10);

        verify(appointmentsRepository, times(1)).findAllByPatientId(0, pageable);
    }

    @Test
    public void shouldApproveAppointmentsWhenDoctorOwnsAppointments() {
        List<Appointment> appointments = List.of(
                Appointment.builder().doctorId(1).build(),
                Appointment.builder().doctorId(1).build(),
                Appointment.builder().doctorId(1).build());
        AppointmentIdsDto dto = new AppointmentIdsDto(List.of(1,2,3));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                1,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        when(appointmentsRepository.findAllById(anyList())).thenReturn(appointments);
        when(appointmentsRepository.saveAll(anyList())).thenReturn(appointments);

        AppointmentListDto appointmentListDto = underTest.approveAppointments(dto, auth);

        assertEquals(3, appointmentListDto.appointments().size());

        verify(appointmentsRepository, times(1)).findAllById(List.of(1,2,3));
        verify(appointmentsRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void shouldCancelAppointmentsWhenDoctorOwnsAppointments() {
        List<Appointment> appointments = List.of(
                Appointment.builder().doctorId(1).build(),
                Appointment.builder().doctorId(1).build(),
                Appointment.builder().doctorId(1).build());
        AppointmentIdsDto dto = new AppointmentIdsDto(List.of(1,2,3));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                1,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        when(appointmentsRepository.findAllById(anyList())).thenReturn(appointments);
        when(appointmentsRepository.saveAll(anyList())).thenReturn(appointments);

        AppointmentListDto appointmentListDto = underTest.cancelAppointments(dto, auth);

        assertEquals(3, appointmentListDto.appointments().size());

        verify(appointmentsRepository, times(1)).findAllById(List.of(1,2,3));
        verify(appointmentsRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void shouldFilterOutOtherDoctorsAppointments() {
        List<Appointment> appointments = List.of(
                Appointment.builder().status(AppointmentStatus.PENDING).doctorId(1).build(),
                Appointment.builder().status(AppointmentStatus.PENDING).doctorId(2).build(),
                Appointment.builder().status(AppointmentStatus.PENDING).doctorId(2).build());
        AppointmentIdsDto dto = new AppointmentIdsDto(List.of(1,2,3));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                1,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );
        when(appointmentsRepository.findAllById(anyList())).thenReturn(appointments);

        // approving appointments
        Stream<Appointment> appointmentStream = underTest.setAppointmentsToScheduled(dto, auth);

        List<Appointment> appointmentList = appointmentStream
                .peek(ap -> assertEquals(AppointmentStatus.SCHEDULED, ap.getStatus()))
                .toList();
        assertEquals(1, appointmentList.size());

        // canceling appointments
        Stream<Appointment> canceledAppointmentStream = underTest.setAppointmentsToCanceled(dto, auth);

        List<Appointment> canceledAppointmentList = canceledAppointmentStream
                .peek(ap -> assertEquals(AppointmentStatus.CANCELED, ap.getStatus()))
                .toList();
        assertEquals(1, canceledAppointmentList.size());
    }

    @Test
    public void shouldOnlySetAppointmentsThatArePending() {
        List<Appointment> appointments = List.of(
                Appointment.builder().status(AppointmentStatus.PENDING).doctorId(1).build(),
                Appointment.builder().status(AppointmentStatus.PENDING).doctorId(1).build(),
                Appointment.builder().status(AppointmentStatus.CANCELED).doctorId(1).build());
        AppointmentIdsDto dto = new AppointmentIdsDto(List.of(1,2,3));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                1,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );
        when(appointmentsRepository.findAllById(anyList())).thenReturn(appointments);

        Stream<Appointment> appointmentStream = underTest.setAppointmentsToScheduled(dto, auth);

        List<Appointment> appointmentList = appointmentStream
                .peek(ap -> assertEquals(AppointmentStatus.SCHEDULED, ap.getStatus()))
                .toList();
        assertEquals(2, appointmentList.size());
    }

    @Test
    public void shouldOnlyCancelActiveOrPendingAppointments() {
        List<Appointment> appointments = List.of(
                Appointment.builder().status(AppointmentStatus.PENDING).doctorId(1).build(),
                Appointment.builder().status(AppointmentStatus.SCHEDULED).doctorId(1).build(),
                Appointment.builder().status(AppointmentStatus.CANCELED).doctorId(1).build());
        AppointmentIdsDto dto = new AppointmentIdsDto(List.of(1,2,3));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                1,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );
        when(appointmentsRepository.findAllById(anyList())).thenReturn(appointments);

        Stream<Appointment> appointmentStream = underTest.setAppointmentsToCanceled(dto, auth);

        List<Appointment> appointmentList = appointmentStream
                .peek(ap -> assertEquals(AppointmentStatus.CANCELED, ap.getStatus()))
                .toList();
        assertEquals(2, appointmentList.size());
    }

    @Test
    public void shouldRescheduleAppointment() {
        LocalDateTime initial = LocalDateTime.now();
        LocalDateTime newTime = initial.plusHours(1);
        Appointment appointment = Appointment.builder()
                .appointmentId(1)
                .dateTime(initial)
                .patientId(0)
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                0,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );

        when(appointmentsRepository.findById(1)).thenReturn(Optional.of(appointment));
        when(appointmentsRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArguments()[0]);

        AppointmentDto dto = underTest.rescheduleAppointment(1, newTime, auth);

        assertEquals(dto.getDateTime(), newTime);
        verify(appointmentsRepository, times(1)).findById(1);
        verify(appointmentsRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    public void shouldThrowExceptionIfUserDoesNotOwnAppointment() {
        LocalDateTime initial = LocalDateTime.now();
        LocalDateTime newTime = initial.plusHours(1);
        Appointment appointment = Appointment.builder()
                .appointmentId(1)
                .dateTime(initial)
                .patientId(0)
                .doctorId(1)
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                1,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );
        when(appointmentsRepository.findById(1)).thenReturn(Optional.of(appointment));

        assertThrows(AuthorizationDeniedException.class,
                () -> underTest.rescheduleAppointment(1, newTime, auth));
        verify(appointmentsRepository, times(1)).findById(1);
        verify(appointmentsRepository, times(0)).save(any());
    }

    @Test
    public void shouldThrowExceptionIfUserHasTheWrongRole() {
        LocalDateTime initial = LocalDateTime.now();
        LocalDateTime newTime = initial.plusHours(1);
        Appointment appointment = Appointment.builder()
                .appointmentId(1)
                .dateTime(initial)
                .patientId(0)
                .doctorId(1)
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                1,
                null,
                List.of()
        );
        when(appointmentsRepository.findById(1)).thenReturn(Optional.of(appointment));

        assertThrows(AuthorizationDeniedException.class,
                () -> underTest.rescheduleAppointment(1, newTime, auth));
        verify(appointmentsRepository, times(1)).findById(1);
        verify(appointmentsRepository, times(0)).save(any());
    }
}