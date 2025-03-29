package io.geraldaddo.hc.patients_service.services;

import io.geraldaddo.hc.patients_service.dtos.UserProfileDto;
import io.geraldaddo.hc.user_data_module.entities.User;
import io.geraldaddo.hc.user_data_module.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientsService {
    @Autowired
    UserRepository userRepository;

    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id: " + id + "does not exits."));
    }

    public void deactivateUser(int id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    public User updateUser(int id, UserProfileDto userProfileDto) {
        User user = getUserById(id);
        user.setFirstName(userProfileDto.getFirstName())
                .setLastName(userProfileDto.getLastName())
                .setAge(userProfileDto.getAge())
                .setNumber(userProfileDto.getNumber())
                .setEmail(userProfileDto.getEmail())
                .setDateOfBirth(userProfileDto.getDateOfBirth())
                .setAddressLineOne(userProfileDto.getAddressLineOne())
                .setAddressLineTwo(userProfileDto.getAddressLineTwo())
                .setCity(userProfileDto.getCity())
                .setCounty(userProfileDto.getCounty())
                .setCountry(userProfileDto.getCountry())
                .setEmergencyFirstName(userProfileDto.getEmergencyFirstName())
                .setEmergencyLastName(userProfileDto.getEmergencyLastName())
                .setEmergencyNumber(userProfileDto.getEmergencyNumber())
                .setPostCode(userProfileDto.getPostCode())
                .setJoined(userProfileDto.getJoined())
                .setInsuranceNumber(userProfileDto.getInsuranceNumber());
        return userRepository.save(user);
    }
}
