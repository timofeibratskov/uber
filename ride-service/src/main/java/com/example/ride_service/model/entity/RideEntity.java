package com.example.ride_service.model.entity;

import com.example.ride_service.model.enums.CancelInitiator;
import com.example.ride_service.model.enums.RideStatus;
import com.example.ride_service.model.enums.converter.CancelInitiatorConverter;
import com.example.ride_service.model.enums.converter.RideStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.geo.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ride_table")
public class RideEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false)
    private UUID passengerId;

    private UUID driverId;

    private String driverName;

    @Column(nullable = false)
    private String startAddress;

    @Column(nullable = false)
    private String stopAddress;

    @Column(nullable = false)
    private Point startPoint;

    @Column(nullable = false)
    private Point stopPoint;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal finalAmount;

    @Column(nullable = false)
    private Integer seats;

    private UUID carId;

    private String carLicensePlate;

    private String carModel;

    private String carBrand;

    private String carColor;

    @Column(nullable = false)
    private String polyline;

    @Column(nullable = false)
    @Convert(converter = RideStatusConverter.class)
    private RideStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Convert(converter = CancelInitiatorConverter.class)
    private CancelInitiator cancelInitiator;

    private LocalDateTime cancelAt;

    private String cancelReasonComment;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}