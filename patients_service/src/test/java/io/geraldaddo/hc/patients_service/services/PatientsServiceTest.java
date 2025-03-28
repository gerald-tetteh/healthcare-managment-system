package io.geraldaddo.hc.patients_service.services;

import io.geraldaddo.hc.patients_service.configurations.PatientsServiceTestConfiguration;
import io.geraldaddo.hc.user_data_module.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import(PatientsServiceTestConfiguration.class)
class PatientsServiceTest {

    @Autowired
    PatientsService patientsService;
    @MockitoBean
    UserRepository userRepository;

    @Test
    void getUserById() {
    }

    @Test
    void deactivateUser() {
    }
}