package com.example.ride_service.client;

import com.example.ride_service.model.dto.OpenRouteRequestDto;
import com.example.ride_service.model.dto.OpenRouteResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "openRoute", url = "${ors.url}")
public interface OpenRouteServiceClient {
    @PostMapping("/latest")
    OpenRouteResponseDto getGeoPath(
            @RequestParam("${ors.api.key}") String apiKey,
            @RequestBody OpenRouteRequestDto body
    );
}