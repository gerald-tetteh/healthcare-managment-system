package io.geraldaddo.hc.patients_service.services;

import io.geraldaddo.hc.patients_service.configurations.PatientsServiceTestConfiguration;
import io.geraldaddo.hc.patients_service.dtos.UserProfileDto;
import io.geraldaddo.hc.user_data_module.entities.User;
import io.geraldaddo.hc.user_data_module.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(PatientsServiceTestConfiguration.class)
class PatientsServiceTest {
    @Autowired
    PatientsService patientsService;
    @MockitoBean
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(new User()
                        .setUserId(0)
                        .setActive(true)
                        .setFirstName("Test")
                        .setLastName("User")
                        .setNumber("+443801385938")));
    }

    @Test
    void getUserById() {
        User user = patientsService.getUserById(0);
        assertEquals(user.getUserId(), 0);
        verify(userRepository, times(1)).findById(0);
    }

    @Test
    void deactivateUser() {
        patientsService.deactivateUser(0);
        verify(userRepository, times(1)).findById(0);
        verify(userRepository, times(1)).save(new User()
                .setUserId(0)
                .setActive(false));
    }

    @Test
    void updateUser() {
        User expectedUser = new User()
                .setUserId(0)
                .setFirstName("AlternateTest")
                .setLastName("User")
                .setNumber("+443801385938");
        UserProfileDto dto = new UserProfileDto()
                .setFirstName("AlternateTest")
                .setLastName("User")
                .setNumber("+443801385938");
        when(userRepository.save(expectedUser)).thenReturn(expectedUser);
        patientsService.updateUser(0, dto);
        verify(userRepository, times(1)).findById(0);
        verify(userRepository, times(1)).save(expectedUser);
    }
}