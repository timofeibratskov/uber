package com.example.payment_service;

import com.example.payment_service.dto.CardRequestDto;
import com.example.payment_service.repo.CardRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TransactionControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepo repo;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        repo.deleteAll();
        CardRequestDto passengerCard = new CardRequestDto(
                "1111-1111-1111-1111",
                BigDecimal.valueOf(1000.00),
                1111
        );

        mockMvc.perform(post("/api/cards/PASSENGER/1/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passengerCard)))
                .andExpect(status().isOk());

        // Создаем карту для водителя (User ID = 2)
        CardRequestDto driverCard = new CardRequestDto(
                "2222-2222-2222-2222",
                BigDecimal.valueOf(500.00),
                2222
        );

        mockMvc.perform(post("/api/cards/DRIVER/2/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverCard)))
                .andExpect(status().isOk());
    }

    @Test
    void testFindAllTransactions() throws Exception {
        mockMvc.perform(get("/transaction/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testFindTransactionsByStatus() throws Exception {
        mockMvc.perform(get("/transaction/all/status/COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testDeleteTransaction() throws Exception {
        mockMvc.perform(delete("/transaction/del/{id}", 1L))
                .andExpect(status().isOk());
    }
}
