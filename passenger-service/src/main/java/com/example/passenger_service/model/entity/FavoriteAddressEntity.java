package com.example.passenger_service.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "favourite_address_table")
public class FavoriteAddressEntity {
    @Id
    @Column(unique = true, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private UUID passengerId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;
}
