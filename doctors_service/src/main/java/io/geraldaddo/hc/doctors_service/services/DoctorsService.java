package io.geraldaddo.hc.doctors_service.services;

import io.geraldaddo.hc.doctors_service.dto.DoctorsAvailabilityDto;
import io.geraldaddo.hc.doctors_service.entities.CurrentStatus;
import io.geraldaddo.hc.user_data_module.entities.Availability;
import io.geraldaddo.hc.user_data_module.entities.DoctorProfile;
import io.geraldaddo.hc.user_data_module.repositories.DoctorProfileRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DoctorsService {
    Logger logger = LogManager.getLogger(DoctorsService.class);
    @Autowired
    private DoctorProfileRepository profileRepository;

    public DoctorProfile getProfile(int id) {
        return profileRepository.findByUserId(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Profile: %d does not exits", id)));
    }

    @Cacheable(value = "availability", key = "#id")
    public DoctorsAvailabilityDto getAvailability(int id) {
        DoctorProfile profile = getProfile(id);
        if(!profile.getUserProfile().isActive()) {
            throw new IllegalArgumentException(String.format("Profile: %d does not exits", id));
        }
        List<Availability> availability = profile.getAvailabilityList();
        return DoctorsAvailabilityDto.builder()
                .currentStatus(getCurrentStatus(availability))
                .availability(availability)
                .build();
    }

    private CurrentStatus getCurrentStatus(List<Availability> availability) {
        LocalDateTime today = LocalDateTime.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        Stream<Availability> todayAvailability = availability.stream()
                .filter(av -> av.dayOfWeek().equals(dayOfWeek));
        LocalTime time = today.toLocalTime();
        boolean isWorking = todayAvailability.anyMatch(
                av -> !time.isBefore(av.startTime()) && !time.isAfter(av.endTime()));
        // placeholder
        boolean inAppointment = false;
        if(!isWorking) {
            return CurrentStatus.UNAVAILABLE;
        } else if (inAppointment) {
            return CurrentStatus.IN_CONSULTATION;
        }
        return CurrentStatus.AVAILABLE;
    }
}
