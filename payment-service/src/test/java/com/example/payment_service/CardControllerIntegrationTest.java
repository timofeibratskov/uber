package com.example.payment_service;

import com.example.payment_service.dto.CardRequestDto;
import com.example.payment_service.dto.UpdateCardPasswordDto;
import com.example.payment_service.repo.CardRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
public class CardControllerIntegrationTest extends BaseIT {
    @Autowired
    private MockMvc mockMvc; // MockMvc inherited from BaseIT
    @Autowired
    private ObjectMapper objectMapper;

    private CardRequestDto validCardRequest;
    private Long cardId;
    @Autowired
    private CardRepo repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        validCardRequest = new CardRequestDto(
                "1111-1111-1111-1111",
                BigDecimal.valueOf(1000.00),
                1111
        );
    }


    @Test
    public void createCard_ShouldReturnCardResponseDto() throws Exception {
        mockMvc.perform(post("/api/cards/PASSENGER/1/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isOk())
                .andReturn();


        mockMvc.perform(get("/api/cards/find/PASSENGER/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value(validCardRequest.cardNumber()))  // Проверяем номер карты
                .andExpect(jsonPath("$.balance").value(validCardRequest.balance()))  // Проверяем баланс, если это необходимо
                .andExpect(jsonPath("$.role").value("PASSENGER"));  // Проверка роли (если требуется)
    }


    @Test
    public void findCardByCardId_ShouldReturnCardResponseDto() throws Exception {
        mockMvc.perform(post("/api/cards/PASSENGER/1/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isOk())
                .andReturn();


        mockMvc.perform(get("/api/cards/find/PASSENGER/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value(validCardRequest.cardNumber()))  // Проверяем номер карты
                .andExpect(jsonPath("$.balance").value(validCardRequest.balance()))  // Проверяем баланс, если это необходимо
                .andExpect(jsonPath("$.role").value("PASSENGER"));  // Проверка роли (если требуется)
    }

    @Test
    public void findCardByOwnerAndRole_ShouldReturnCardResponseDto() throws Exception {
        mockMvc.perform(post("/api/cards/PASSENGER/1/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/api/cards/find/PASSENGER/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value(validCardRequest.cardNumber()))  // Проверяем номер карты
                .andExpect(jsonPath("$.balance").value(validCardRequest.balance()))  // Проверяем баланс, если это необходимо
                .andExpect(jsonPath("$.role").value("PASSENGER"));  // Проверка роли (если требуется)
    }

    @Test
    public void getBalance_ShouldReturnBalance() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/cards/DRIVER/1/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = createResult.getResponse().getContentAsString();
        var cardId = JsonPath.read(jsonResponse, "$.id");  // Получаем ID из JSON-ответа

        mockMvc.perform(get("/api/cards/{cardId}/getBalance", cardId)
                        .param("password", "1111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1000.0));  // Check the balance is 1000.0
    }

    @Test
    public void updateCardPassword_ShouldReturnSuccessMessage() throws Exception {
        // Создание карты перед изменением пароля
        MvcResult createResult = mockMvc.perform(post("/api/cards/DRIVER/1/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract card ID and set it for future tests
        String jsonResponse = createResult.getResponse().getContentAsString();
        var id = JsonPath.read(jsonResponse, "$.id");

        // Обновление пароля карты
        UpdateCardPasswordDto updateCardPasswordDto = new UpdateCardPasswordDto(1111, 5678);
        mockMvc.perform(patch("/api/cards/{id}/updatePassword", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCardPasswordDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Пароль изменен успешно!"));

        // Проверяем, что пароль был обновлен
        mockMvc.perform(get("/api/cards/"+id+"/getBalance")
                        .param("password", "5678"))  // Новый пароль
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1000.0));  // Проверяем баланс
    }

    @Test
    public void deleteCard_ShouldDeleteCardSuccessfully() throws Exception {
        // Создание карты перед удалением
        MvcResult createResult = mockMvc.perform(post("/api/cards/DRIVER/1/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract card ID and set it for future tests
        String jsonResponse = createResult.getResponse().getContentAsString();
        var cardId = JsonPath.read(jsonResponse, "$.id");

        // Удаление карты
        mockMvc.perform(delete("/api/cards/{cardId}/delete", cardId)
                        .param("password", "1111"))
                .andExpect(status().isOk());
    }
}
