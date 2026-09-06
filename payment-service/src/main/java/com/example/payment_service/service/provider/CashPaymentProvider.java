package com.example.payment_service.service.provider;

import com.example.payment_service.model.entity.PaymentEntity;
import com.example.payment_service.model.entity.PaymentMethodEntity;
import com.example.payment_service.model.entity.PaymentTransactionEntity;
import com.example.payment_service.model.enums.PaymentStatus;
import com.example.payment_service.model.enums.PaymentType;
import com.example.payment_service.model.event.DriverSearchingEvent;
import com.example.payment_service.model.event.PaymentCapturedEvent;
import com.example.payment_service.service.OutboxService;
import com.example.payment_service.service.PaymentService;
import com.example.payment_service.service.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CashPaymentProvider
        implements PaymentProvider {
    @Value("${spring.kafka.topic.rides.payment}")
    private String ridePaymentTopic;
    @Value("${spring.kafka.event.payment-authorized}")
    private String successfulAuthEventName;
    @Value("${spring.kafka.event.payment-captured}")
    private String capturedEventName;
    private final OutboxService outboxService;
    private final PaymentService paymentService;
    private final PaymentTransactionService transactionService;

    @Override
    public PaymentType getType() {
        return PaymentType.CASH;
    }

    @Override
    @Transactional
    public void authorize(
            PaymentMethodEntity method,
            BigDecimal amount,
            String currency,
            UUID rideId,
            Integer seats,
            Point startPoint) {

        var payment = PaymentEntity.createPaymentEntity(
                rideId,
                method.getId(),
                "cash-intent-" + UUID.randomUUID(),
                amount,
                currency
        );

        var transaction = PaymentTransactionEntity.createForAuthorize(payment);
        transactionService.save(transaction);


        payment.setStatus(PaymentStatus.AUTHORIZED);
        paymentService.save(payment);

        outboxService.saveEvent(
                DriverSearchingEvent.create(rideId, seats, startPoint),
                successfulAuthEventName,
                ridePaymentTopic
        );
    }

    @Override
    @Transactional
    public void capture(PaymentEntity payment) {

        var transaction = PaymentTransactionEntity.createForCapture(payment);

        var oEvent = PaymentCapturedEvent.createEvent(transaction, payment);
        outboxService.saveEvent(oEvent, capturedEventName, ridePaymentTopic);

    }

    @Override
    public void release(PaymentEntity payment) {
        //empty
    }
}
