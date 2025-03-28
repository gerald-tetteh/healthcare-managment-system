package io.geraldaddo.hc.patients_service.services;

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
}
