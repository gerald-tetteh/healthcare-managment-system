package io.geraldaddo.hc.doctors_service.services;

import io.geraldaddo.hc.doctors_service.configurations.DoctorsServiceTestConfiguration;
import io.geraldaddo.hc.doctors_service.dto.DoctorsAvailabilityDto;
import io.geraldaddo.hc.doctors_service.entities.CurrentStatus;
import io.geraldaddo.hc.user_data_module.entities.Availability;
import io.geraldaddo.hc.user_data_module.entities.DoctorProfile;
import io.geraldaddo.hc.user_data_module.entities.User;
import io.geraldaddo.hc.user_data_module.repositories.DoctorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(DoctorsServiceTestConfiguration.class)
class DoctorsServiceTest {
    @MockitoBean
    DoctorProfileRepository profileRepository;
    @Autowired
    DoctorsService doctorsService;

    final List<Availability> availability = List.of(
            new Availability(
                    LocalTime.parse("09:00:00"),
                    LocalTime.parse("17:00:00"),
                    DayOfWeek.MONDAY),
            new Availability(
                    LocalTime.parse("11:00:00"),
                    LocalTime.parse("22:00:00"),
                    DayOfWeek.WEDNESDAY)
    );
    final DoctorProfile doctorProfile = new DoctorProfile()
            .setProfileId(0L)
            .setAvailabilityList(availability)
            .setConsultationFee(453.93)
            .setSpecialisation("Brain Surgery")
            .setLicenseNumber("234-292-494")
            .setUserProfile(new User().setActive(true));

    @BeforeEach
    void setUp() {
        when(profileRepository.findByUserId(0)).thenReturn(Optional.of(doctorProfile));
    }

    @Test
    void getProfile() {
    }

    @Test
    void testCurrentStatusShouldShowUnavailable() {
        // same day but outside work hours
        LocalDateTime day = LocalDateTime.parse("2025-04-02T09:04:00");
        DoctorsAvailabilityDto availability = doctorsService.getAvailability(0, day);

        assert(availability.getCurrentStatus() == CurrentStatus.UNAVAILABLE);
        verify(profileRepository,times(1)).findByUserId(0);

        // different day but inside work hours
        LocalDateTime day2 = LocalDateTime.parse("2025-04-03T13:04:00");
        DoctorsAvailabilityDto availability2 = doctorsService.getAvailability(0, day2);

        assert(availability2.getCurrentStatus() == CurrentStatus.UNAVAILABLE);
        verify(profileRepository,times(2)).findByUserId(0);
    }

    @Test
    void testCurrentStatusShouldShowAvailable() {
        LocalDateTime day = LocalDateTime.parse("2025-04-02T13:04:00");
        DoctorsAvailabilityDto availability = doctorsService.getAvailability(0, day);

        assert(availability.getCurrentStatus() == CurrentStatus.AVAILABLE);
        verify(profileRepository,times(1)).findByUserId(0);
    }
}