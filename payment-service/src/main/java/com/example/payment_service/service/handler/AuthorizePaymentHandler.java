package com.example.payment_service.service.handler;

import com.example.payment_service.exception.PaymentMethodNotFoundException;
import com.example.payment_service.model.entity.PaymentEntity;
import com.example.payment_service.model.entity.PaymentTransactionEntity;
import com.example.payment_service.model.enums.PaymentStatus;
import com.example.payment_service.model.enums.TransactionStatus;
import com.example.payment_service.model.event.AuthorizeFailedEvent;
import com.example.payment_service.model.event.PaymentAuthorizedEvent;
import com.example.payment_service.model.event.RideCreatedEvent;
import com.example.payment_service.service.OutboxService;
import com.example.payment_service.service.PaymentMethodService;
import com.example.payment_service.service.PaymentService;
import com.example.payment_service.service.PaymentTransactionService;
import com.example.payment_service.service.StripeGatewayService;
import com.example.payment_service.util.MinorUntilConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthorizePaymentHandler {
    private final PaymentMethodService paymentMethodService;
    private final PaymentTransactionService transactionService;
    private final PaymentService paymentService;
    private final StripeGatewayService paymentGateway;
    private final OutboxService outboxService;

    @Value("${spring.kafka.topic.rides.payment}")
    private String ridePaymentTopic;
    @Value("${spring.kafka.event.payment-authorization-failed}")
    private String authFailedEventName;
    @Value("${spring.kafka.event.payment-authorized}")
    private String successfulAuthEventName;

    @Transactional
    public void handle(RideCreatedEvent event) {
        var paymentMethod = paymentMethodService.findById(event.paymentMethodId())
                .orElseThrow(() -> new PaymentMethodNotFoundException("Payment Method Not Found"));

        paymentMethodService.findAllByUserId(event.passengerId())
                .stream()
                .filter(m -> m.id().equals(paymentMethod.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Payment method does not belong to the user"));

        long amountInMinorUnits = MinorUntilConverter.convert(event.amount(), event.currency());

        var result = paymentGateway.authorize(
                amountInMinorUnits,
                event.currency(),
                paymentMethod.getExternalToken()
        );

        var payment = PaymentEntity.createPaymentEntity(
                event.rideId(),
                paymentMethod.getId(),
                result.intentId(),
                event.amount(),
                event.currency()
        );


        var transaction = PaymentTransactionEntity.createForAuthorize(payment, result);
        transactionService.save(transaction);


        payment.setStatus(result.isSuccess() ? PaymentStatus.AUTHORIZED : PaymentStatus.FAILED);
        paymentService.save(payment);

        boolean flag = transaction.getStatus().equals(TransactionStatus.SUCCESS);

        var newEvent = flag ?
                PaymentAuthorizedEvent.create(event) :
                AuthorizeFailedEvent.create(event, transaction.getErrorMessage());

        outboxService.saveEvent(
                newEvent,
                flag ? successfulAuthEventName : authFailedEventName,
                ridePaymentTopic
        );
    }
}