package io.geraldaddo.hc.doctors_service.services;

import io.geraldaddo.hc.user_data_module.entities.Availability;
import io.geraldaddo.hc.user_data_module.entities.DoctorProfile;
import io.geraldaddo.hc.user_data_module.repositories.DoctorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorsService {
    @Autowired
    private DoctorProfileRepository profileRepository;

    public DoctorProfile getProfile(int id) {
        return profileRepository.findByUserId(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Profile: %d does not exits", id)));
    }

    @Cacheable(value = "availability", key = "#id")
    public List<Availability> getAvailability(int id) {
        DoctorProfile profile = getProfile(id);
        if(!profile.getUserProfile().isActive()) {
            throw new IllegalArgumentException(String.format("Profile: %d does not exits", id));
        }
        return profile.getAvailabilityList();
    }
}
