package com.example.passenger_service;

import com.example.passenger_service.dto.LoginPassengerRequest;
import com.example.passenger_service.dto.PassengerRequest;
import com.example.passenger_service.entity.PassengerEntity;
import com.example.passenger_service.repo.PassengerRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PassengerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PassengerRepo passengerRepo;

    private PassengerEntity testPassenger;

    @BeforeEach
    void setUp() {
        passengerRepo.deleteAllInBatch();
        testPassenger = passengerRepo.save(
                new PassengerEntity(
                        null,
                        "Existing User",
                        "dup@gmail.com",
                        "validPass",
                        "+79111234567",
                        4.5f
                )
        );
    }

    // Регистрация
    @Test
    void registerPassenger_ValidData_ReturnsCreated() throws Exception {
        PassengerRequest request = createValidRequest();
        performRegistration(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("АККАУНТ УСПЕШНО СОЗДАН!"));
    }

    @Test
    void registerPassenger_DuplicateEmail_ReturnsConflict() throws Exception {
        PassengerRequest request = new PassengerRequest(
                "New User",
                "dup@gmail.com",
                "newPass",
                "+79001112233"
        );
        performRegistration(request)
                .andExpect(status().isConflict());
    }

    @Test
    void registerPassenger_DuplicateName_ReturnsConflict() throws Exception {
        PassengerRequest request = new PassengerRequest(
                "Existing User",
                "new@gmail.com",
                "newPass",
                "+79001112233"
        );
        performRegistration(request)
                .andExpect(status().isConflict());
    }

    @Test
    void registerPassenger_DuplicatePhone_ReturnsConflict() throws Exception {
        PassengerRequest request = new PassengerRequest(
                "New User",
                "new@gmail.com",
                "newPass",
                "+79111234567"
        );
        performRegistration(request)
                .andExpect(status().isConflict());
    }

    @Test
    void registerPassenger_InvalidEmail_ReturnsBadRequest() throws Exception {
        PassengerRequest request = new PassengerRequest(
                "User",
                "invalid-email", // Невалидный email
                "pass",
                "+79111234567"
        );

        mockMvc.perform(post("/api/passengers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Ожидаем 400
                .andExpect(jsonPath("$.errors[?(@.field == 'gmail')].message")
                        .value("Почта в неверном формате!"));
    }

    @Test
    void registerPassenger_EmptyName_ReturnsBadRequest() throws Exception {
        PassengerRequest request = new PassengerRequest(
                "",
                "email@test.com",
                "pass",
                "+79111234567"
        );

        mockMvc.perform(post("/api/passengers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'name')].message")
                        .value("Имя пользователя обязательно!"));
    }

    // Авторизация
    @Test
    void loginPassenger_ValidCredentials_ReturnsOk() throws Exception {
        LoginPassengerRequest request = new LoginPassengerRequest(
                "dup@gmail.com",
                "validPass"
        );
        performLogin(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gmail").value("dup@gmail.com"));
    }

    @Test
    void loginPassenger_InvalidPassword_ReturnsEx() throws Exception {
        LoginPassengerRequest request = new LoginPassengerRequest(
                "dup@gmail.com",
                "wrongPass"
        );
        performLogin(request)
                .andExpect(status().isConflict());
    }

    // Обновление данных
    @Test
    void updatePassenger_ValidData_ReturnsOk() throws Exception {
        PassengerRequest request = new PassengerRequest(
                "Updated Name",
                "updated@gmail.com",
                "newPass123",
                "+79001112233"
        );
        performUpdate(testPassenger.getId(), request)
                .andExpect(status().isOk())
                .andExpect(content().string("ОБНОВЛЕНО УСПЕШНО"));
    }

    @Test
    void updatePassenger_DuplicateEmail_ReturnsConflict() throws Exception {
        passengerRepo.save(createTestPassenger("another@gmail.com"));
        PassengerRequest request = new PassengerRequest(
                "New Name",
                "another@gmail.com",
                "pass",
                "+79001112233"
        );
        performUpdate(testPassenger.getId(), request)
                .andExpect(status().isConflict());
    }

    // Удаление
    @Test
    void deletePassenger_ValidId_ReturnsNoContent() throws Exception {
        performDelete(testPassenger.getId())
                .andExpect(status().isNoContent());
        assertFalse(passengerRepo.existsById(testPassenger.getId()));
    }

    @Test
    void deletePassenger_InvalidId_ReturnsNotFound() throws Exception {
        performDelete(999L)
                .andExpect(status().isNotFound());
    }

    private PassengerRequest createValidRequest() {
        return new PassengerRequest(
                "New User",
                "new@gmail.com",
                "newPassword123",
                "+79117654321"
        );
    }

    private ResultActions performRegistration(PassengerRequest request) throws Exception {
        return mockMvc.perform(post("/api/passengers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performLogin(LoginPassengerRequest request) throws Exception {
        return mockMvc.perform(post("/api/passengers/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performUpdate(Long id, PassengerRequest request) throws Exception {
        return mockMvc.perform(put("/api/passengers/update/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performDelete(Long id) throws Exception {
        return mockMvc.perform(delete("/api/passengers/delete/{id}", id));
    }

    private PassengerEntity createTestPassenger(String email) {
        return passengerRepo.save(new PassengerEntity(
                null,
                "Test User",
                email,
                "password",
                "+79001112233",
                4.0f
        ));
    }
}