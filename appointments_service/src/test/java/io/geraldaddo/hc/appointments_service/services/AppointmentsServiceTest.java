package io.geraldaddo.hc.appointments_service.services;

import io.geraldaddo.hc.appointments_service.configurations.AppointmentsTestConfiguration;
import io.geraldaddo.hc.appointments_service.dto.CreateAppointmentDto;
import io.geraldaddo.hc.appointments_service.entities.Appointment;
import io.geraldaddo.hc.appointments_service.repositories.AppointmentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import({AppointmentsTestConfiguration.class})
class AppointmentsServiceTest {
    @MockitoBean
    AppointmentsRepository appointmentsRepository;
    @Autowired
    AppointmentsService underTest;

    @Test
    public void shouldCreateAppointment() {
        CreateAppointmentDto createAppointmentDto = CreateAppointmentDto.builder()
                .doctorId(0)
                .patientId(0)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .build();
        when(appointmentsRepository.countClashingAppointments(
                anyInt(),anyInt(),any(LocalDateTime.class),any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(appointmentsRepository.save(any(Appointment.class)))
                .thenReturn(new Appointment());

        underTest.createAppointment(createAppointmentDto);

        verify(appointmentsRepository, times(1))
                .countClashingAppointments(anyInt(),anyInt(),any(LocalDateTime.class),any(LocalDateTime.class));
        verify(appointmentsRepository, times(1))
                .save(any(Appointment.class));
    }

    @Test
    public void shouldThrowExceptionForClashingAppointment() {
        CreateAppointmentDto createAppointmentDto = CreateAppointmentDto.builder()
                .doctorId(0)
                .patientId(0)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .build();
        when(appointmentsRepository.countClashingAppointments(
                anyInt(),anyInt(),any(LocalDateTime.class),any(LocalDateTime.class)))
                .thenReturn(List.of(new Appointment()));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            underTest.createAppointment(createAppointmentDto);
        });
        assertEquals("Appointment clashes with other appointments", exception.getMessage());

        verify(appointmentsRepository, times(1))
                .countClashingAppointments(anyInt(),anyInt(),any(LocalDateTime.class),any(LocalDateTime.class));
        verify(appointmentsRepository, times(0))
                .save(any(Appointment.class));
    }

    @Test
    public void shouldThrowExceptionForStartDateAfterEndDate() {
        CreateAppointmentDto createAppointmentDto = CreateAppointmentDto.builder()
                .doctorId(0)
                .patientId(0)
                .startDateTime(LocalDateTime.now().plusHours(1))
                .endDateTime(LocalDateTime.now())
                .build();
        when(appointmentsRepository.countClashingAppointments(
                anyInt(),anyInt(),any(LocalDateTime.class),any(LocalDateTime.class)))
                .thenReturn(List.of());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            underTest.createAppointment(createAppointmentDto);
        });
        assertEquals("Appointment end date is before start date", exception.getMessage());

        verify(appointmentsRepository, times(1))
                .countClashingAppointments(anyInt(),anyInt(),any(LocalDateTime.class),any(LocalDateTime.class));
        verify(appointmentsRepository, times(0))
                .save(any(Appointment.class));
    }
}