package com.example.driver_service.service

import com.example.driver_service.exception.CarLimitExceededException
import com.example.driver_service.exception.CarNotFoundException
import com.example.driver_service.exception.LicensePlateAlreadyExistsException
import com.example.driver_service.mapper.CarMapper
import com.example.driver_service.model.dto.CarResponseDto
import com.example.driver_service.model.dto.CreateCarDto
import com.example.driver_service.model.dto.UpdateCarDto
import com.example.driver_service.model.entity.CarEntity
import com.example.driver_service.repository.CarRepository
import java.util.UUID
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CarService(
    private val carRepository: CarRepository,
    private val carMapper: CarMapper
) {
    companion object {
        private const val MAX_CARS_PER_DRIVER = 3
        private val log = KotlinLogging.logger {}
    }

    @Transactional(readOnly = true)
    fun findAllByDriverId(driverId: UUID): List<CarResponseDto> {
        log.info { "Fetching all cars for driver: $driverId" }
        return carRepository.findByDriverId(driverId)
            .map(carMapper::toDto)
    }

    @Transactional(readOnly = true)
    fun findByCarIdAndDriverId(carId: UUID, driverId: UUID): CarResponseDto {
        log.info { "Fetching car $carId for driver $driverId" }
        val car = carRepository.findByCarIdAndDriverId(carId, driverId)
            ?: throw CarNotFoundException("Car with ID $carId not found for driver $driverId").also {
                log.error { "Car not found: ${it.message}" }
            }
        return carMapper.toDto(car)
    }

    @Transactional
    fun add(driverId: UUID, createCarDto: CreateCarDto): CarEntity {
        log.info { "Attempting to add car with plate ${createCarDto.licensePlate} for driver $driverId" }

        val cars = carRepository.findByDriverId(driverId)
        if (cars.size >= MAX_CARS_PER_DRIVER) {
            log.warn { "Driver $driverId reached car limit ($MAX_CARS_PER_DRIVER)" }
            throw CarLimitExceededException("The driver is limited to only $MAX_CARS_PER_DRIVER cars")
        }

        val car = carRepository.findByLicensePlate(createCarDto.licensePlate)
        return if (car == null) {
            val newCar = carMapper.toEntity(driverId, createCarDto)
            carRepository.save(newCar)
            log.info { "Successfully created new car with ID ${newCar.id}" }
            newCar
        } else {
            if (car.isDeleted) {
                car.driverId = driverId
                car.isDeleted = false
                carRepository.update(car)
                log.info { "Restored previously deleted car with ID ${car.id} for driver $driverId" }
                car
            } else {
                log.warn { "License plate ${createCarDto.licensePlate} already in use" }
                throw LicensePlateAlreadyExistsException("Car with license plate: ${createCarDto.licensePlate} already exists!")
            }
        }
    }

    @Transactional
    fun softDeleteById(id: UUID) {
        log.info { "Soft deleting car with ID: $id" }
        carRepository.softDeleteById(id)
    }

    @Transactional
    fun update(driverId: UUID, carId: UUID, dto: UpdateCarDto): CarResponseDto {
        log.info { "Updating car $carId for driver $driverId" }

        var car = carRepository.findByCarIdAndDriverId(carId, driverId)
            ?: throw CarNotFoundException("Car not found").also {
                log.error { "Update failed: ${it.message}" }
            }

        car = carMapper.updateEntity(dto, car)
        carRepository.update(car)

        log.info { "Successfully updated car $carId" }
        return carMapper.toDto(car)
    }
}