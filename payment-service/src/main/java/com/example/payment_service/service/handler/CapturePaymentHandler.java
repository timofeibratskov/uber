package com.example.payment_service.service.handler;

import com.example.payment_service.exception.PaymentNotFoundException;
import com.example.payment_service.model.entity.PaymentTransactionEntity;
import com.example.payment_service.model.enums.PaymentStatus;
import com.example.payment_service.model.event.PaymentCaptureFailedEvent;
import com.example.payment_service.model.event.PaymentCapturedEvent;
import com.example.payment_service.model.event.RideCompletedEvent;
import com.example.payment_service.service.OutboxService;
import com.example.payment_service.service.PaymentService;
import com.example.payment_service.service.PaymentTransactionService;
import com.example.payment_service.service.StripeGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CapturePaymentHandler {
    private final StripeGatewayService gateway;
    private final PaymentService paymentService;
    private final PaymentTransactionService transactionService;
    private final OutboxService outboxService;

    @Value("${spring.kafka.topic.rides.payment}")
    private String ridePaymentTopic;
    @Value("${spring.kafka.event.payment-capture-failed}")
    private String paymentCaptureFailedEventName;
    @Value("${spring.kafka.event.payment-captured}")
    private String paymentCapturedEventName;

    @Transactional
    public void handle(RideCompletedEvent event) {
        var payment = paymentService.findByRideId(event.rideId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            log.info("Payment for ride {} has incorrect status for capturing. Skipping.", event.rideId());
            return;
        }

        if (payment.getIntentId() == null)
            throw new RuntimeException("Payment with id=" + payment.getId() + " without intent!");

        var result = gateway.capture(payment.getIntentId());

        var transaction = PaymentTransactionEntity.createForCapture(payment, result);

        if (result.isSuccess()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            var oEvent = PaymentCapturedEvent.createEvent(transaction, payment);
            outboxService.saveEvent(oEvent, paymentCapturedEventName, ridePaymentTopic);

        } else {
            payment.setStatus(PaymentStatus.FAILED);
            var oEvent = PaymentCaptureFailedEvent.createEvent(transaction, payment);
            outboxService.saveEvent(oEvent, paymentCaptureFailedEventName, ridePaymentTopic);
        }

        payment.setNew(false);
        paymentService.save(payment);
        transactionService.save(transaction);
    }
}
