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

@RestController
@RequestMapping("/api/cars")
class CarController(
    private val carService: CarService,
    private val carMapperDto: CarMapper
) {

    @GetMapping("/all")
    fun getAllCars(): List<CarDto> {
        val cars = carService.getAll()
        return cars.map { carMapperDto.toDto(it) }
    }

    @GetMapping("/{id}")
    fun getCar(@PathVariable id: Long): CarDto {
        val car = carService.getCarById(id)
        return carMapperDto.toDto(car)
    }

    @PostMapping("/create/{driverId}")
    fun createCar(@PathVariable driverId: Long, @RequestBody carDto: CarDto): CarDto {
        val entity = carMapperDto.toEntity(carDto).apply { this.driverId = driverId }
        val createdCar = carService.createCar(entity)
        return carMapperDto.toDto(createdCar)
    }

    @PutMapping("/update/{id}")
    fun updateCar(@PathVariable id: Long, @RequestBody carDto: CarDto): CarDto {
        val entity = carMapperDto.toEntity(carDto).apply { this.id = id }
        val updatedCar = carService.updateCar(entity)
        return carMapperDto.toDto(updatedCar)
    }

    @DeleteMapping("/delete/{id}")
    fun deleteCar(@PathVariable id: Long) {
        carService.deleteCar(id)
    }
}
