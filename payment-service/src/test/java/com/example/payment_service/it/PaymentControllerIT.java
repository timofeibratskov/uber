package com.example.payment_service.it;

import com.example.payment_service.application.dto.PaymentRequest;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.TransactionStatus;
import com.example.payment_service.domain.repository.PaymentMethodRepository;
import com.example.payment_service.domain.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerIT extends BaseIT {

    @Autowired
    private PaymentMethodRepository methodRepository;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Test
    @DisplayName("успешная оплата наличными")
    void shouldProcessCashPaymentSuccessfully() throws Exception {
        // arrange
        UUID userId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        PaymentMethod cashMethod = PaymentMethod.createCashMethod(userId);
        methodRepository.insert(cashMethod);

        // act
        var request = PaymentRequest.builder()
                .userId(userId)
                .rideId(rideId)
                .amount(new BigDecimal("15.00"))
                .currency("USD")
                .paymentMethodId(cashMethod.getId())
                .build();

        // assert
        mockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var transaction = transactionRepository.findByRideId(rideId);

        assertThat(transaction).isPresent();
        assertEquals(TransactionStatus.SUCCESS, transaction.get().getStatus());
        assertEquals(0, new BigDecimal("15.00").compareTo(transaction.get().getAmount().amount()));
    }
}