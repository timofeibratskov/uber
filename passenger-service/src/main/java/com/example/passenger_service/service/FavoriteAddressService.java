package com.example.passenger_service.service;

import com.example.passenger_service.exception.FavoriteAddressLimitException;
import com.example.passenger_service.exception.FavoriteAddressNotFoundException;
import com.example.passenger_service.mapper.FavoriteAddressMapper;
import com.example.passenger_service.model.dto.FavoriteAddressRequestDto;
import com.example.passenger_service.model.dto.FavoriteAddressResponseDto;
import com.example.passenger_service.repo.FavoriteAddressRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class FavoriteAddressService {
    private static final int MAX_FAVORITES_ADDRESSES = 5;
    private final FavoriteAddressRepo favoriteAddressRepo;
    private final FavoriteAddressMapper favoriteAddressMapper;

    @Transactional(readOnly = true)
    public List<FavoriteAddressResponseDto> getAllAddressesByPassengerId(UUID id) {
        log.info("Fetching all favorite address by passenger id {}", id);
        return favoriteAddressRepo.findByPassengerId(id)
                .stream()
                .map(favoriteAddressMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public FavoriteAddressResponseDto addFavoriteAddress(UUID passengerId,
                                                         FavoriteAddressRequestDto favoriteAddressRequestDto) {
        var addresses = favoriteAddressRepo.findByPassengerId(passengerId);
        if (addresses.size() < MAX_FAVORITES_ADDRESSES) {
            var entity = favoriteAddressMapper.toEntity(passengerId, favoriteAddressRequestDto);
            favoriteAddressRepo.save(entity);
            log.info("Saving favorite address with passengerId: {}", entity.getPassengerId());
            return favoriteAddressMapper.toResponseDto(entity);
        } else {
            log.info("favorite address is too much");
            throw new FavoriteAddressLimitException("favorite addresses is too much!");
        }
    }

    @Transactional
    public void removeFavoriteAddress(UUID passengerId, UUID addressId) {
        int deletedCount = favoriteAddressRepo.deleteByIdAndPassengerId(addressId, passengerId);
        if (deletedCount > 0) {
            log.info("Successfully deleted favorite address for passenger id: {}", passengerId);
        } else {
            log.info("Failed to delete: address {} not found for passenger {}", addressId, passengerId);
            throw new FavoriteAddressNotFoundException("Favorite address not found for passenger id: " + passengerId);
        }
    }
}