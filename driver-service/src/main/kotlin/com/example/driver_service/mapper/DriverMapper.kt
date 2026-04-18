package com.example.driver_service.mapper

import com.example.driver_service.model.dto.DriverResponseDto
import com.example.driver_service.model.dto.RegisterDriverDto
import com.example.driver_service.model.entity.DriverEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface DriverMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    fun toEntity(driver: RegisterDriverDto): DriverEntity

    fun toDto(driver: DriverEntity): DriverResponseDto
}