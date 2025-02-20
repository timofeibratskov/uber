package com.example.driver_service.mapper

import com.example.driver_service.dto.DriverDto
import com.example.driver_service.dto.RegistrationDriverDto
import com.example.driver_service.entity.DriverEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface DriverMapper {
    fun toDto(entity: DriverEntity): DriverDto

    fun toEntity(dto: DriverDto): DriverEntity

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", constant = "0.0f")
    fun fromRegistrationDto(dto: RegistrationDriverDto): DriverEntity
}