package com.example.payment_service.entity;

import com.example.payment_service.enums.Role;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("card")
@ToString
public class CardEntity {

    @Id
    private Long id;

    @Pattern(regexp = "^\\d{4}-\\d{4}-\\d{4}-\\d{4}$")
    private String cardNumber;

    @Positive
    private BigDecimal balance;

    @Min(1000)
    @Max(9999)
    private Integer password;

    @Column("owner_id")
    private Long ownerId;

    @Column
    private Role role;
}
