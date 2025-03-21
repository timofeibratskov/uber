package com.example.driver_service.mapper

import com.example.driver_service.dto.CarDto
import com.example.driver_service.entity.CarEntity
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface CarMapper {
    fun toDto(entity: CarEntity): CarDto
    fun toEntity(dto: CarDto): CarEntity
}