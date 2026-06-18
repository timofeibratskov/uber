package com.example.payment_service.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public class MinorUntilConverter {
    public static long convert(BigDecimal amount, String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        int fractionDigits = currency.getDefaultFractionDigits();

        return amount.multiply(BigDecimal.TEN.pow(fractionDigits))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }
}