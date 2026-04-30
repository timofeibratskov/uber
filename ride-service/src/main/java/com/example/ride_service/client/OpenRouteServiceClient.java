package com.example.ride_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenRouteServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ors.api.key}")
    private String apiKey;

    @Value("${ors.url}")
    private String orsUrl;

    public JsonNode fetchRoute(Point startPoint, Point endPoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "coordinates", List.of(
                        List.of(startPoint.getX(), startPoint.getY()),
                        List.of(endPoint.getX(), endPoint.getY())
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(orsUrl, entity, JsonNode.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch route from ORS, {}",e.getMessage());
            throw new RuntimeException("Unable to calculate route at this time");
        }
    }
}