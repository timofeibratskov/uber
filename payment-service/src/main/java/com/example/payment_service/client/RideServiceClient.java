package com.example.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "ride-service", url = "http://localhost:8083/api/rides")
public interface RideServiceClient {

    @PutMapping("/{rideId}/pay")
    String payRide(@PathVariable String rideId);

}
