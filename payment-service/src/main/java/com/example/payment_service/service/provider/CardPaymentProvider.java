package com.example.payment_service.service.provider;

import com.example.payment_service.model.entity.PaymentEntity;
import com.example.payment_service.model.entity.PaymentMethodEntity;
import com.example.payment_service.model.entity.PaymentTransactionEntity;
import com.example.payment_service.model.enums.PaymentStatus;
import com.example.payment_service.model.enums.PaymentType;
import com.example.payment_service.model.enums.TransactionStatus;
import com.example.payment_service.model.event.AuthorizeFailedEvent;
import com.example.payment_service.model.event.DriverSearchingEvent;
import com.example.payment_service.model.event.PaymentCaptureFailedEvent;
import com.example.payment_service.model.event.PaymentCapturedEvent;
import com.example.payment_service.service.OutboxService;
import com.example.payment_service.service.PaymentService;
import com.example.payment_service.service.PaymentTransactionService;
import com.example.payment_service.service.gateway.GatewayAdapter;
import com.example.payment_service.util.MinorUntilConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardPaymentProvider implements PaymentProvider {
    @Value("${spring.kafka.topic.rides.payment}")
    private String ridePaymentTopic;
    @Value("${spring.kafka.event.payment-authorization-failed}")
    private String authFailedEventName;
    @Value("${spring.kafka.event.payment-authorized}")
    private String successfulAuthEventName;
    @Value("${spring.kafka.event.payment-captured}")
    private String capturedEventName;
    @Value("${spring.kafka.event.payment-capture-failed}")
    private String captureFailedEventName;
    private final GatewayAdapter gatewayAdapter;
    private final OutboxService outboxService;
    private final PaymentService paymentService;
    private final PaymentTransactionService transactionService;

    @Override
    public PaymentType getType() {
        return PaymentType.CARD;
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

        long amountInMinorUnits = MinorUntilConverter.convert(amount, currency);

        var result = gatewayAdapter.authorize(
                amountInMinorUnits,
                currency,
                method.getExternalToken()
        );

        var payment = PaymentEntity.createPaymentEntity(
                rideId,
                method.getId(),
                result.intentId(),
                amount,
                currency
        );

        var transaction = PaymentTransactionEntity.createForAuthorize(payment, result);
        transactionService.save(transaction);


        payment.setStatus(result.isSuccess() ? PaymentStatus.AUTHORIZED : PaymentStatus.FAILED);
        paymentService.save(payment);

        boolean flag = transaction.getStatus().equals(TransactionStatus.SUCCESS);

        outboxService.saveEvent(
                flag ? DriverSearchingEvent.create(rideId, seats, startPoint) :
                        AuthorizeFailedEvent.create(rideId, transaction.getErrorMessage()),
                flag ? successfulAuthEventName :
                        authFailedEventName,
                ridePaymentTopic
        );

    }

    @Override
    @Transactional
    public void capture(PaymentEntity payment) {

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            log.info("Payment {} for  has incorrect status for capturing. Skipping.", payment.getRideId());
            return;
        }

        if (payment.getIntentId() == null)
            throw new RuntimeException("Payment with id=" + payment.getId() + " without intent!");

        var result = gatewayAdapter.capture(payment.getIntentId());

        var transaction = PaymentTransactionEntity.createForCapture(payment, result);

        if (result.isSuccess()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            var oEvent = PaymentCapturedEvent.createEvent(transaction, payment);
            outboxService.saveEvent(oEvent, capturedEventName, ridePaymentTopic);

        } else {
            payment.setStatus(PaymentStatus.FAILED);
            var oEvent = PaymentCaptureFailedEvent.createEvent(transaction, payment);
            outboxService.saveEvent(oEvent, captureFailedEventName, ridePaymentTopic);
        }

        payment.setNew(false);
        paymentService.save(payment);
        transactionService.save(transaction);
    }

    @Override
    @Transactional
    public void release(PaymentEntity payment) {

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            log.info("Payment for ride {} has incorrect status for releasing. Skipping.", payment.getRideId());
            return;
        }

        var result = gatewayAdapter.release(payment.getIntentId());

        var transaction = PaymentTransactionEntity.createForRelease(payment, result);
        transactionService.save(transaction);

        payment.setStatus(result.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setNew(false);

        paymentService.save(payment);
    }
}
