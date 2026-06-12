package com.example.passenger_service.controller;

import com.example.passenger_service.model.dto.FavoriteAddressRequestDto;
import com.example.passenger_service.model.dto.FavoriteAddressResponseDto;
import com.example.passenger_service.model.dto.LoginPassengerDto;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.RegisterPassengerDto;
import com.example.passenger_service.model.dto.UpdatePassengerDto;
import com.example.passenger_service.service.FavoriteAddressService;
import com.example.passenger_service.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/passengers")
public class PassengerController {
    private final PassengerService passengerService;
    private final FavoriteAddressService favoriteAddressService;

    @PostMapping("/register")
    public ResponseEntity<String> registerPassenger(
            @RequestBody @Valid RegisterPassengerDto request) {
        return ResponseEntity.status(201).body(passengerService.registerPassenger(request));
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginPassenger(
            @RequestBody @Valid LoginPassengerDto request) {
        return ResponseEntity.ok().body(passengerService.loginPassenger(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponseDto> getPassengerById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(passengerService.findPassengerById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> updatePassenger(@PathVariable UUID id,
                                                  @RequestBody @Valid UpdatePassengerDto request) {
        passengerService.updatePassenger(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<FavoriteAddressResponseDto> deleteFavoriteAddress(@PathVariable UUID id,
                                                                            @RequestBody @Valid FavoriteAddressRequestDto request) {
        return ResponseEntity.status(201).body(favoriteAddressService.addFavoriteAddress(id, request));
    }

    @GetMapping("/{id}/addresses")
    public ResponseEntity<List<FavoriteAddressResponseDto>> getFavoriteAddress(@PathVariable UUID id) {
        return ResponseEntity.ok().body(favoriteAddressService.getAllAddressesByPassengerId(id));

    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<Void> deleteFavoriteAddress(@PathVariable UUID id,
                                                      @PathVariable UUID addressId) {
        favoriteAddressService.removeFavoriteAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }
}