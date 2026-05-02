package com.example.payment_service.infrastructure.payment;

import com.example.payment_service.domain.exception.StripeServiceException;
import com.example.payment_service.domain.gateway.PaymentGateway;
import com.stripe.StripeClient;
import com.stripe.param.AccountCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeAccountAdapter implements PaymentGateway {
    private final StripeClient stripeClient;

    @Override
    public String createAccount(String email) {
        try {
            var params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setEmail(email)
                    .setCapabilities(
                            AccountCreateParams.Capabilities.builder()
                                    .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                            .setRequested(true).build())
                                    .build())
                    .build();

            var account = stripeClient.v1().accounts().create(params);
            log.info("Successfully created Stripe account for email: {}", email);
            return account.getId();
        } catch (Exception e) {
            log.error("Stripe error for email {}: {}", email, e.getMessage());
            throw new StripeServiceException("Stripe service failure when creating account");
        }
    }
}
