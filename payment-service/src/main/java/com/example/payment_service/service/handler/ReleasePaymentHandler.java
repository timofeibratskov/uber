package com.example.payment_service.service.handler;

import com.example.payment_service.exception.PaymentNotFoundException;
import com.example.payment_service.model.entity.PaymentTransactionEntity;
import com.example.payment_service.model.enums.PaymentStatus;
import com.example.payment_service.model.event.RideCanceledEvent;
import com.example.payment_service.service.PaymentService;
import com.example.payment_service.service.PaymentTransactionService;
import com.example.payment_service.service.StripeGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReleasePaymentHandler {
    private final PaymentTransactionService transactionService;
    private final PaymentService paymentService;
    private final StripeGatewayService stripeGatewayService;

    @Transactional
    public void handle(RideCanceledEvent event) {
        var payment = paymentService.findByRideId(event.rideId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            log.info("Payment for ride {} has incorrect status for capturing. Skipping.", event.rideId());
            return;
        }

        var result = stripeGatewayService.release(payment.getIntentId());

        var transaction = PaymentTransactionEntity.createForRelease(payment, result);
        transactionService.save(transaction);

        payment.setStatus(result.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setNew(false);

        paymentService.save(payment);
    }
}
