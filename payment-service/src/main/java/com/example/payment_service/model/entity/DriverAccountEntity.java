package com.example.payment_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "driver_account_table")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverAccountEntity
        implements Persistable<UUID> {

    @Id
    @Column("driver_id")
    private UUID driverId;
    private String accountId;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return getDriverId();
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}