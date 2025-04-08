package io.geraldaddo.hc.appointments_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.appointments_service.configurations.AppointmentsTestConfiguration;
import io.geraldaddo.hc.appointments_service.dto.CreateAppointmentDto;
import io.geraldaddo.hc.appointments_service.dto.DoctorAvailableDto;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;

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
                .thenReturn(new Appointment());
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

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            underTest.createAppointment(createAppointmentDto, "");
        });
        assertEquals("Doctor is unavailable at specified time", exception.getMessage());

        verify(appointmentsRepository, times(0))
                .save(any(Appointment.class));
    }

    @Test
    public void shouldThrowExceptionIfDoctorServiceUnavailable() throws IOException {
        CreateAppointmentDto createAppointmentDto = CreateAppointmentDto.builder()
                .doctorId(0)
                .patientId(0)
                .dateTime(LocalDateTime.now())
                .notes("test notes")
                .build();
        mockWebServer.enqueue(new MockResponse());

        Exception exception = assertThrows(AppointmentsServerException.class, () -> {
            underTest.createAppointment(createAppointmentDto, "");
        });
        assertEquals("Could not validate doctors availability", exception.getMessage());

        verify(appointmentsRepository, times(0))
                .save(any(Appointment.class));
    }
}