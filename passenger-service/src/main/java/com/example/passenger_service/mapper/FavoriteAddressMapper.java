package com.example.passenger_service.mapper;

import com.example.passenger_service.model.dto.FavoriteAddressRequestDto;
import com.example.passenger_service.model.dto.FavoriteAddressResponseDto;
import com.example.passenger_service.model.entity.FavoriteAddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FavoriteAddressMapper {
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    FavoriteAddressEntity toEntity(UUID passengerId, FavoriteAddressRequestDto favoriteAddressRequestDto);

    FavoriteAddressResponseDto toResponseDto(FavoriteAddressEntity favoriteAddressEntity);
}
