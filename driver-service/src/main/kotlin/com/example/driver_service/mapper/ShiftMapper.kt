package com.example.driver_service.mapper

import com.example.driver_service.model.dto.CarShortResponseDto
import com.example.driver_service.model.dto.ShiftDto
import com.example.driver_service.model.entity.ShiftEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface ShiftMapper {

    @Mapping(target = "id", source = "shiftEntity.id")
    @Mapping(target = "carDto", source = "car")
    fun toDto(shiftEntity: ShiftEntity, car: CarShortResponseDto): ShiftDto
}