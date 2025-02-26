package com.example.payment_service.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("card")
public class CardEntity {
    @Id
    private Long id;
    @Pattern(regexp = "^\\d{4}-\\d{4}-\\d{4}-\\d{4}$", message = "Номер карты должен быть в формате XXXX-XXXX-XXXX-XXXX")
    private String cardNumber;
    @Positive
    private BigDecimal balance;
    @Min(value = 1000, message = "Пароль должен состоять из 4 цифр")
    @Max(value = 9999, message = "Пароль должен состоять из 4 цифр")
    private Integer password;
}
