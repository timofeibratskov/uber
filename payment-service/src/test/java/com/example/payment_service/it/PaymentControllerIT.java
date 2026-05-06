package com.example.payment_service.it;

import com.example.payment_service.application.dto.CreatePaymentRequest;
import com.example.payment_service.domain.model.DriverAccount;
import com.example.payment_service.domain.model.EventType;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.TopicType;
import com.example.payment_service.domain.model.TransactionStatus;
import com.example.payment_service.infrastructure.client.RideServiceClient;
import com.example.payment_service.infrastructure.persistence.DriverAccountRepositoryImpl;
import com.example.payment_service.infrastructure.persistence.OutboxRepositoryImpl;
import com.example.payment_service.infrastructure.persistence.PaymentMethodRepositoryImpl;
import com.example.payment_service.infrastructure.persistence.PaymentTransactionRepositoryImpl;
import com.stripe.StripeClient;
import com.stripe.exception.CardException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.service.PaymentIntentService;
import com.stripe.service.V1Services;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerIT extends BaseIT {

    @Autowired
    private PaymentMethodRepositoryImpl methodRepository;

    @Autowired
    private PaymentTransactionRepositoryImpl transactionRepository;

    @Autowired
    private DriverAccountRepositoryImpl driverAccountRepository;

    @Autowired
    private OutboxRepositoryImpl outboxRepository;

    @MockitoBean
    private StripeClient stripeClient;

    @MockitoBean
    private RideServiceClient rideServiceClient;

    @BeforeEach
    public void setup() {
        transactionRepository.deleteAll();
        driverAccountRepository.deleteAll();
        methodRepository.deleteAll();
        outboxRepository.deleteAll();
    }

    @Test
    @DisplayName("успешная оплата наличными")
    void shouldProcessCashPaymentSuccessfully() throws Exception {
        // arrange
        UUID passengerId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        PaymentMethod cashMethod = PaymentMethod.createCashMethod(passengerId);
        methodRepository.insert(cashMethod);

        when(rideServiceClient.canPayRide(any()))
                .thenReturn(ResponseEntity.ok(true));

        // act
        var request = CreatePaymentRequest.builder()
                .passengerId(passengerId)
                .driverId(driverId)
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

        var outboxes = outboxRepository.findAllByOrderByCreatedAt();
        assertEquals(1, outboxes.size());

        var outbox = outboxes.getFirst();

        assertEquals(rideId, objectMapper.readValue(outbox.getPayload(), UUID.class));
        assertEquals(TopicType.PAYMENT, outbox.getTopic());
        assertEquals(EventType.PAYMENT_COMPLETED, outbox.getEventType());
    }

    @Test
    @DisplayName("успешная оплата по карте")
    void shouldProcessCardPaymentSuccessfully() throws Exception {
        // arrange
        UUID passengerId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        PaymentMethod cashMethod = PaymentMethod.createCardMethod(passengerId, "pm_card_visa");
        methodRepository.insert(cashMethod);


        when(rideServiceClient.canPayRide(any()))
                .thenReturn(ResponseEntity.ok(true));

        var driverAccount = DriverAccount.builder()
                .driverId(driverId)
                .accountId("acct_test_12345")
                .build();

        driverAccountRepository.insert(driverAccount);

        V1Services mockV1 = mock(V1Services.class);
        PaymentIntentService mockIntents = mock(PaymentIntentService.class);
        PaymentIntent mockIntent = mock(PaymentIntent.class);
        when(stripeClient.v1()).thenReturn(mockV1);
        when(mockV1.paymentIntents()).thenReturn(mockIntents);
        when(mockIntents.create(any())).thenReturn(mockIntent);
        when(mockIntent.getStatus()).thenReturn("succeeded");

        var request = CreatePaymentRequest.builder()
                .passengerId(passengerId)
                .driverId(driverId)
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

        var outboxes = outboxRepository.findAllByOrderByCreatedAt();
        assertEquals(1, outboxes.size());

        var outbox = outboxes.getFirst();

        assertEquals(rideId, objectMapper.readValue(outbox.getPayload(), UUID.class));
        assertEquals(TopicType.PAYMENT, outbox.getTopic());
        assertEquals(EventType.PAYMENT_COMPLETED, outbox.getEventType());
    }

    @Test
    @DisplayName("ошибка оплаты: недостаточно средств на карте")
    void shouldFailPaymentWhenInsufficientFunds() throws Exception {
        // arrange
        UUID passengerId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();

        PaymentMethod cardMethod = PaymentMethod.createCardMethod(passengerId, "pm_card_insufficientFunds");
        methodRepository.insert(cardMethod);

        when(rideServiceClient.canPayRide(any()))
                .thenReturn(ResponseEntity.ok(true));

        var driverAccount = DriverAccount.builder()
                .driverId(driverId)
                .accountId("acct_test_failed")
                .build();
        driverAccountRepository.insert(driverAccount);

        V1Services mockV1 = mock(V1Services.class);
        PaymentIntentService mockIntents = mock(PaymentIntentService.class);
        when(stripeClient.v1()).thenReturn(mockV1);
        when(mockV1.paymentIntents()).thenReturn(mockIntents);

        var cardException = new CardException(
                "Your card has insufficient funds.",
                "req_123",
                "card_declined",
                "amount",
                "insufficient_funds",
                "ch_123",
                402,
                null
        );

        when(mockIntents.create(any(PaymentIntentCreateParams.class)))
                .thenThrow(cardException);

        var request = CreatePaymentRequest.builder()
                .passengerId(passengerId)
                .driverId(driverId)
                .rideId(rideId)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .paymentMethodId(cardMethod.getId())
                .build();

        // act
        mockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // assert
        var transaction = transactionRepository.findByRideId(rideId);

        assertThat(transaction).isPresent();
        var tr = transaction.get();

        assertEquals(TransactionStatus.FAILED, tr.getStatus());

        assertEquals(passengerId, tr.getPassengerId());
        assertEquals(driverId, tr.getDriverId());

        assertEquals(0, outboxRepository.findAllByOrderByCreatedAt().size());
    }

    @Test
    @DisplayName("ошибка оплаты: поездка не завершена")
    void shouldFailPaymentWhenRideNotCompleted() throws Exception {
        // arrange
        UUID passengerId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        PaymentMethod cashMethod = PaymentMethod.createCashMethod(passengerId);
        methodRepository.insert(cashMethod);

        when(rideServiceClient.canPayRide(rideId))
                .thenReturn(ResponseEntity.ok(false));

        var request = CreatePaymentRequest.builder()
                .passengerId(passengerId)
                .driverId(UUID.randomUUID())
                .rideId(rideId)
                .amount(new BigDecimal("15.00"))
                .currency("USD")
                .paymentMethodId(cashMethod.getId())
                .build();

        // act
        mockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // assert
        assertThat(transactionRepository.findByRideId(rideId)).isEmpty();
        assertEquals(0, outboxRepository.findAllByOrderByCreatedAt().size());
    }
}