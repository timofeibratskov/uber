package com.example.payment_service.it;

import com.example.payment_service.application.dto.CreatePaymentMethodRequest;
import com.example.payment_service.application.dto.CreatePaymentRequest;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentType;
import com.example.payment_service.domain.model.TransactionStatus;
import com.example.payment_service.domain.repository.PaymentMethodRepository;
import com.example.payment_service.domain.repository.PaymentTransactionRepository;
import com.stripe.StripeClient;
import com.stripe.model.PaymentIntent;
import com.stripe.service.PaymentIntentService;
import com.stripe.service.V1Services;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerIT extends BaseIT {

    @Autowired
    private PaymentMethodRepository methodRepository;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @MockitoBean
    private StripeClient stripeClient;

    @Test
    @DisplayName("успешная оплата наличными")
    void shouldProcessCashPaymentSuccessfully() throws Exception {
        // arrange
        UUID userId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        PaymentMethod cashMethod = PaymentMethod.createCashMethod(userId);
        methodRepository.insert(cashMethod);

        // act
        var request = CreatePaymentRequest.builder()
                .userId(userId)
                .rideId(rideId)
                .amount(new BigDecimal("15.00"))
                .currency("USD")
                .paymentMethodId(cashMethod.getId())
                .build();

        // act

        mockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        // assert

        var transaction = transactionRepository.findByRideId(rideId);

        assertThat(transaction).isPresent();
        assertEquals(TransactionStatus.SUCCESS, transaction.get().getStatus());
        assertEquals(0, new BigDecimal("15.00").compareTo(transaction.get().getAmount().amount()));
    }

    @Test
    @DisplayName("успешная оплата по карте")
    void shouldProcessCardPaymentSuccessfully() throws Exception {
        // arrange
        UUID userId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        PaymentMethod cashMethod = PaymentMethod.createCardMethod(userId, "pm_card_visa");
        methodRepository.insert(cashMethod);

        V1Services mockV1 = mock(V1Services.class);
        PaymentIntentService mockIntents = mock(PaymentIntentService.class);
        PaymentIntent mockIntent = mock(PaymentIntent.class);
        when(stripeClient.v1()).thenReturn(mockV1);
        when(mockV1.paymentIntents()).thenReturn(mockIntents);
        when(mockIntents.create(any())).thenReturn(mockIntent);
        when(mockIntent.getStatus()).thenReturn("succeeded");

        var request = CreatePaymentRequest.builder()
                .userId(userId)
                .rideId(rideId)
                .amount(new BigDecimal("15.00"))
                .currency("USD")
                .paymentMethodId(cashMethod.getId())
                .build();

        // act
        mockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // assert
        var transaction = transactionRepository.findByRideId(rideId);

        assertThat(transaction).isPresent();
        assertEquals(TransactionStatus.SUCCESS, transaction.get().getStatus());
        assertEquals(0, new BigDecimal("15.00").compareTo(transaction.get().getAmount().amount()));
    }

    @Test
    @DisplayName("успешная привязка карты")
    void shouldCreateCardPaymentMethodSuccessfully() throws Exception {
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
}