package com.example.payment_service.service;

import com.example.payment_service.exception.StripeServiceException;
import com.example.payment_service.model.dto.GatewayAuthorizationResult;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeGatewayService {

    private final StripeClient stripeClient;

    public String createAccount(String email) {
        try {
            var params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setEmail(email)
                    .build();

            return stripeClient.v1().accounts().create(params).getId();
        } catch (StripeException e) {
            log.error("Stripe account creation failed for {}: {}", email, e.getMessage());
            throw new StripeServiceException("Stripe service failure");
        }
    }

    public GatewayAuthorizationResult authorize(
            Long amountInCents,
            String currency,
            String paymentMethodStripeToken
    ) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency)
                    .setPaymentMethod(paymentMethodStripeToken)
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    .setConfirm(true)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            PaymentIntent intent = stripeClient.v1().paymentIntents().create(params);
            return buildSuccessResult(intent);
        } catch (StripeException e) {
            return handleStripeException(e);
        }
    }

    public GatewayAuthorizationResult capture(String intentId) {
        try {
            PaymentIntent intent = stripeClient.v1().paymentIntents().capture(intentId);
            return buildSuccessResult(intent);
        } catch (StripeException e) {
            return handleStripeException(e);
        }
    }

    public GatewayAuthorizationResult release(String intentId) {
        try {
            PaymentIntent intent = stripeClient.v1().paymentIntents().cancel(intentId);
            return buildSuccessResult(intent);
        } catch (StripeException e) {
            return handleStripeException(e);
        }
    }

    private GatewayAuthorizationResult buildSuccessResult(PaymentIntent intent) {
        var charge = intent.getLatestChargeObject();

        return GatewayAuthorizationResult.builder()
                .isSuccess(true)
                .intentId(intent.getId())
                .chargeId(charge != null ? charge.getId() : null)
                .build();
    }

    private GatewayAuthorizationResult handleStripeException(StripeException e) {
        log.error("Stripe error [{}]: code={}, message={}",
                e.getClass().getSimpleName(), e.getCode(), e.getMessage());

        return GatewayAuthorizationResult.builder()
                .isSuccess(false)
                .errorMessage(e.getMessage())
                .build();
    }
}