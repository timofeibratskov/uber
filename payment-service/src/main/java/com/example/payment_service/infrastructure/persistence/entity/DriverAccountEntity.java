package com.example.payment_service.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "driver_account_table")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverAccountEntity {
    @Id
    private UUID driverId;
    private String accountId;
}
