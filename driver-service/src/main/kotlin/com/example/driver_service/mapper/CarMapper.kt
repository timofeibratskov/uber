package com.example.driver_service.mapper

import com.example.driver_service.model.dto.CarResponseDto
import com.example.driver_service.model.dto.CreateCarDto
import com.example.driver_service.model.dto.UpdateCarDto
import com.example.driver_service.model.entity.CarEntity
import java.util.UUID
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy

@Mapper(componentModel = "spring")
interface CarMapper {
    fun toDto(entity: CarEntity): CarResponseDto


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "driverId", source = "driverId")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    fun toEntity(driverId: UUID, dto: CreateCarDto): CarEntity

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun updateEntity(dto: UpdateCarDto, @MappingTarget entity: CarEntity): CarEntity
}