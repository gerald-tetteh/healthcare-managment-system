package io.geraldaddo.hc.patients_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.patients_service.dtos.UserProfileDto;
import io.geraldaddo.hc.patients_service.services.PatientsService;
import io.geraldaddo.hc.user_data_module.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientsController.class)
class PatientsControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    PatientsService patientsService;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final User user = new User()
            .setFirstName("Test")
            .setLastName("User")
            .setRoles(List.of())
            .setActive(true)
            .setUserId(0);

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                0,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @Test
    void getUserProfile() throws Exception {
        UserProfileDto profileDto = new UserProfileDto()
                .setFirstName("Test")
                .setLastName("User");
        String expectedJson = mapper.writeValueAsString(profileDto);
        when(patientsService.getUserById(anyInt())).thenReturn(user);
        mockMvc.perform(get("/patients/0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
        verify(patientsService, times(1)).getUserById(0);
    }

    @Test
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/patients/0").with(csrf()))
                .andExpect(status().isOk());
        verify(patientsService, times(1)).deactivateUser(0);
    }

    @Test
    void updateUser() throws Exception {
        UserProfileDto initialUser = new UserProfileDto()
                .setFirstName("Test")
                .setLastName("User");
        UserProfileDto updatedUserDto = new UserProfileDto()
                .setFirstName("UpdatedTest")
                .setLastName("UpdatedUser");
        User updatedUser = new User()
                .setFirstName("UpdatedTest")
                .setLastName("UpdatedUser");
        String userJson = mapper.writeValueAsString(initialUser);
        when(patientsService.updateUser(anyInt(), any(UserProfileDto.class))).thenReturn(updatedUser);
        mockMvc.perform(put("/patients/0")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(updatedUserDto)));
    }
}