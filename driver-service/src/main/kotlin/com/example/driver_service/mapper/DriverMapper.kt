package com.example.driver_service.mapper

import com.example.driver_service.model.dto.DriverResponseDto
import com.example.driver_service.model.dto.CompleteProfileRequestDto
import com.example.driver_service.model.entity.DriverEntity
import com.example.driver_service.model.event.UserRegisteredEvent
import java.math.BigDecimal
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget

@Mapper(componentModel = "spring")
interface DriverMapper {
    fun updateEntity(
        @MappingTarget mainDriver: DriverEntity,
        driver: CompleteProfileRequestDto
    )

    @Mapping(source = "userId", target = "id")
    fun toEntity(event: UserRegisteredEvent): DriverEntity

    fun toDto(driver: DriverEntity, rating: BigDecimal): DriverResponseDto
}