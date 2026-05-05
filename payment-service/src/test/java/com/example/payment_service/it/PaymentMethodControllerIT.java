package com.example.payment_service.it;

import com.example.payment_service.application.dto.CreatePaymentMethodRequest;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentType;
import com.example.payment_service.infrastructure.persistence.PaymentMethodRepositoryImpl;
import com.stripe.StripeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentMethodControllerIT extends BaseIT {

    @Autowired
    private PaymentMethodRepositoryImpl methodRepository;

    @MockitoBean
    private StripeClient stripeClient;

    @BeforeEach
    public void setup() {
        methodRepository.deleteAll();
    }

    @Test
    @DisplayName("успешная привязка карты")
    void shouldCreateNewCardPaymentMethodSuccessfully() throws Exception {
        // arrange
        var request = CreatePaymentMethodRequest.builder()
                .userId(UUID.randomUUID())
                .paymentType(PaymentType.CARD)
                .externalToken("pm_card_visa")
                .build();


        // act
        mockMvc.perform(post("/api/v1/payment-methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // assert
        var methods = methodRepository.findAllByUserId(request.userId());
        assertNotNull(methods);
        assertEquals(1, methods.size());
        var method = methods.getFirst();
        assertEquals(method.getExternalToken(), request.externalToken());
        assertEquals(method.getUserId(), request.userId());
        assertEquals(method.getType(), request.paymentType());
        assertNotNull(method.getId());
    }

    @Test
    @DisplayName("успешная привязка уже удаленной карты")
    void shouldCreateIsDeletedCardPaymentMethodSuccessfully() throws Exception {
        // arrange
        var userId = UUID.randomUUID();
        var token = "pm_card_visa";

        var dMethod = PaymentMethod.createCardMethod(userId, token);
        dMethod.markAsDeleted();

        methodRepository.insert(dMethod);

        var request = CreatePaymentMethodRequest.builder()
                .userId(userId)
                .paymentType(PaymentType.CARD)
                .externalToken(token)
                .build();

        // act
        mockMvc.perform(post("/api/v1/payment-methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // assert
        var methods = methodRepository.findAllByUserId(request.userId());
        assertNotNull(methods);
        assertEquals(1, methods.size());
        var method = methods.getFirst();
        assertFalse(method.isDeleted());
        assertEquals(method.getExternalToken(), request.externalToken());
        assertEquals(method.getUserId(), request.userId());
        assertEquals(method.getType(), request.paymentType());
        assertNotNull(method.getId());
    }

    @Test
    @DisplayName("ошибка: такая карта уже существует")
    void shouldCardPaymentMethodSuccessfully() throws Exception {
        // arrange
        var userId = UUID.randomUUID();
        var token = "pm_card_visa";

        var dMethod = PaymentMethod.createCardMethod(userId, token);

        methodRepository.insert(dMethod);

        var request = CreatePaymentMethodRequest.builder()
                .userId(userId)
                .paymentType(PaymentType.CARD)
                .externalToken(token)
                .build();

        // act
        mockMvc.perform(post("/api/v1/payment-methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        // assert
        var methods = methodRepository.findAllByUserId(request.userId());
        assertNotNull(methods);
        assertEquals(1, methods.size());
        var method = methods.getFirst();
        assertEquals(method.getExternalToken(), request.externalToken());
        assertFalse(method.isDeleted());
        assertEquals(method.getUserId(), request.userId());
        assertEquals(method.getType(), request.paymentType());
        assertNotNull(method.getId());
    }

    @Test
    @DisplayName("успешное сохранение оплаты наличными")
    void shouldCreateCashPaymentMethodSuccessfully() throws Exception {
        // arrange
        var request = CreatePaymentMethodRequest.builder()
                .userId(UUID.randomUUID())
                .paymentType(PaymentType.CASH)
                .build();


        // act
        mockMvc.perform(post("/api/v1/payment-methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // assert
        var methods = methodRepository.findAllByUserId(request.userId());
        assertNotNull(methods);
        assertEquals(1, methods.size());
        var method = methods.getFirst();
        assertNull(method.getExternalToken());
        assertEquals(method.getUserId(), request.userId());
        assertEquals(method.getType(), request.paymentType());
        assertNotNull(method.getId());
    }

    @Test
    @DisplayName("чтение списка вариатнов оплаты пользователя")
    void shouldGetUserPaymentMethods() throws Exception {
        // arrange
        UUID userId = UUID.randomUUID();
        var cardPaymentMethod = PaymentMethod.createCardMethod(userId, "pm_card_visa");
        methodRepository.insert(cardPaymentMethod);

        // act
        mockMvc.perform(get("/api/v1/payment-methods/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // assert
        var methods = methodRepository.findAllByUserId(userId);
        assertNotNull(methods);
        assertEquals(1, methods.size());
        var method = methods.getFirst();
        assertNotNull(method.getExternalToken());
        assertEquals(method.getUserId(), userId);
        assertEquals(method.getType(), cardPaymentMethod.getType());
        assertNotNull(method.getId());
    }

    @Test
    @DisplayName("успешное удаление оплаты картой")
    void shouldDeleteCardPaymentMethodSuccessfully() throws Exception {
        // arrange
        var paymentMethod = PaymentMethod.createCardMethod(UUID.randomUUID(), "pm_card_visa");
        methodRepository.insert(paymentMethod);


        // act
        mockMvc.perform(delete("/api/v1/payment-methods/{id}", paymentMethod.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // assert
        var optMethod = methodRepository.findById(paymentMethod.getId());
        assertThat(optMethod).isPresent();
        assertTrue(optMethod.get().isDeleted());
    }
}
