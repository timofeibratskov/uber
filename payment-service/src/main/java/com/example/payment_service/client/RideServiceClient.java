package com.example.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "ride-service", url = "http://localhost:8083/api/rides")
public interface RideServiceClient {

    @PutMapping("/{rideId}/pay")
    String payRide(
            @PathVariable("rideId") String rideId,
            @RequestParam("amount") BigDecimal amount
    );
}