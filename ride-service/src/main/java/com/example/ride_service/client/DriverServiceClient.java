package com.example.ride_service.client;

import com.example.ride_service.model.dto.DriverResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(value = "driver-service")
public interface DriverServiceClient {
    @GetMapping("/api/v1/drivers/{driverId}")
    DriverResponseDto getDriverById(@PathVariable UUID driverId);
}
