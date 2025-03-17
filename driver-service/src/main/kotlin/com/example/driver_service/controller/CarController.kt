package com.example.driver_service.controller

import com.example.driver_service.dto.CarDto
import com.example.driver_service.mapper.CarMapper
import com.example.driver_service.service.CarService
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Car Management", description = "APIs for managing driver's cars")
@RestController
@RequestMapping("/api/cars")
class CarController(
    private val carService: CarService,
    private val carMapperDto: CarMapper
) {

    @Operation(
        summary = "Get all cars",
        description = "Retrieves a list of all registered cars",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "List of cars retrieved successfully")
        ])
    @GetMapping("/all")
    fun getAllCars(): List<CarDto> {
        val cars = carService.getAll()
        return cars.map { carMapperDto.toDto(it) }
    }

    @Operation(
        summary = "Get car by ID",
        description = "Retrieves car details by ID",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Car details retrieved successfully"),
            ApiResponse(
                responseCode = "404",
                description = "Car not found")
        ])
    @GetMapping("/{id}")
    fun getCar(
        @Parameter(description = "ID of the car", example = "1")
        @PathVariable id: Long
    ): CarDto {
        val car = carService.getCarById(id)
        return carMapperDto.toDto(car)
    }

    @Operation(
        summary = "Create a new car",
        description = "Registers a new car for a driver",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Car created successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input")
        ])
    @PostMapping("/create/{driverId}")
    fun createCar(
        @Parameter(description = "ID of the driver", example = "1")
        @PathVariable driverId: Long,
        @RequestBody carDto: CarDto
    ): CarDto {
        val entity = carMapperDto.toEntity(carDto).apply { this.driverId = driverId }
        val createdCar = carService.createCar(entity)
        return carMapperDto.toDto(createdCar)
    }

    @Operation(
        summary = "Update car details",
        description = "Updates car information by ID",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Car updated successfully"),
            ApiResponse(
                responseCode = "404",
                description = "Car not found")
        ])
    @PutMapping("/update/{id}")
    fun updateCar(
        @Parameter(description = "ID of the car", example = "1")
        @PathVariable id: Long,
        @RequestBody carDto: CarDto
    ): CarDto {
        val entity = carMapperDto.toEntity(carDto).apply { this.id = id }
        val updatedCar = carService.updateCar(entity)
        return carMapperDto.toDto(updatedCar)
    }

    @Operation(
        summary = "Delete car",
        description = "Deletes a car by ID",
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Car deleted successfully"),
            ApiResponse(
                responseCode = "404",
                description = "Car not found")
        ])
    @DeleteMapping("/delete/{id}")
    fun deleteCar(
        @Parameter(description = "ID of the car", example = "1")
        @PathVariable id: Long
    ) {
        carService.deleteCar(id)
    }
}