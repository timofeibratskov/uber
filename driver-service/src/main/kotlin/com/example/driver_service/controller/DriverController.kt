package com.example.driver_service.controller

import com.example.driver_service.model.dto.CarResponseDto
import com.example.driver_service.model.dto.CreateCarDto
import com.example.driver_service.model.dto.DriverResponseDto
import com.example.driver_service.model.dto.LoginDriverDto
import com.example.driver_service.model.dto.RegisterDriverDto
import com.example.driver_service.model.dto.UpdateCarDto
import com.example.driver_service.model.dto.UpdateDriverDto
import com.example.driver_service.service.CarService
import com.example.driver_service.service.DriverService
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/drivers")
class DriverController(
    private val driverService: DriverService,
    private val carService: CarService
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody dto: RegisterDriverDto):
            ResponseEntity<DriverResponseDto> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(driverService.register(dto))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody dto: LoginDriverDto):
            ResponseEntity<DriverResponseDto> {
        return ResponseEntity.ok(driverService.login(dto))
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID):
            ResponseEntity<DriverResponseDto> {
        return ResponseEntity.ok(driverService.findById(id))
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody dto: UpdateDriverDto
    ): ResponseEntity<DriverResponseDto> {
        return ResponseEntity.ok(driverService.update(id, dto))
    }

    @PostMapping("/{id}/cars")
    fun linkCar(
        @PathVariable id: UUID,
        @Valid @RequestBody dto: CreateCarDto
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(driverService.linkCar(id, dto))
    }

    @GetMapping("/{id}/cars")
    fun getCars(@PathVariable id: UUID):
            ResponseEntity<List<CarResponseDto>> {
        return ResponseEntity.ok(carService.findAllByDriverId(id))
    }

    @GetMapping("/{driverId}/cars/{carId}")
    fun getCar(
        @PathVariable driverId: UUID,
        @PathVariable carId: UUID
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(carService.findByCarIdAndDriverId(carId, driverId))
    }

    @PatchMapping("/{driverId}/cars/{carId}/main")
    fun assignCarAsMain(
        @PathVariable driverId: UUID,
        @PathVariable carId: UUID,
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(driverService.assignCarAsMain(driverId, carId))
    }

    @PatchMapping("/{driverId}/cars/{carId}")
    fun updateCar(
        @PathVariable driverId: UUID,
        @PathVariable carId: UUID,
        @Valid @RequestBody dto: UpdateCarDto
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(carService.update(driverId, carId, dto))
    }

    @DeleteMapping("/{driverId}/cars/{carId}")
    fun deleteCar(
        @PathVariable driverId: UUID,
        @PathVariable carId: UUID,
    ): ResponseEntity<Void> {
        driverService.unlinkCar(driverId, carId)
        return ResponseEntity.noContent().build()
    }
}
