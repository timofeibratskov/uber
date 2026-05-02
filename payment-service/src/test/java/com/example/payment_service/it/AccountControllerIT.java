package com.example.payment_service.it;

import com.example.payment_service.application.dto.CreateDriverAccountRequest;
import com.example.payment_service.domain.repository.DriverAccountRepository;
import com.stripe.StripeClient;
import com.stripe.model.Account;
import com.stripe.param.AccountCreateParams;
import com.stripe.service.AccountService;
import com.stripe.service.V1Services;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountControllerIT extends BaseIT {

    @Autowired
    private DriverAccountRepository driverAccountRepository;

    @MockitoBean
    private StripeClient stripeClient;

    @Test
    @DisplayName("успешное создание аккаунта в страйп")
    void shouldCreateDriverAccountSuccessfully() throws Exception {
        // arrange
        var mockV1 = mock(V1Services.class);
        var mockAccountService = mock(AccountService.class);
        var mockAccount = mock(Account.class);

        String expectedStripeId = "acct_test_12345";

        when(stripeClient.v1()).thenReturn(mockV1);
        when(mockV1.accounts()).thenReturn(mockAccountService);

        when(mockAccountService.create(any(AccountCreateParams.class)))
                .thenReturn(mockAccount);

        when(mockAccount.getId()).thenReturn(expectedStripeId);

        var request = CreateDriverAccountRequest.builder()
                .driverId(UUID.randomUUID())
                .email("user@gmail.com")
                .build();

        // act
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // assert
        var account = driverAccountRepository.findByDriverId(request.driverId());
        assertThat(account).isPresent();
        assertEquals(expectedStripeId, account.get().accountId());
    }
}