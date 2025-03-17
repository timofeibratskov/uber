package com.example.driver_service.controller

import com.example.driver_service.dto.DriverDto
import com.example.driver_service.dto.LoginDriverDto
import com.example.driver_service.dto.RegistrationDriverDto
import com.example.driver_service.mapper.DriverMapper
import com.example.driver_service.service.DriverService
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

@Tag(name = "Driver Management", description = "APIs for managing drivers")
@RestController
@RequestMapping("/api/drivers")
class DriverController(
    private val driverService: DriverService,
    private val driverMapperDto: DriverMapper
) {

    @Operation(
        summary = "Get all drivers",
        description = "Retrieves a list of all registered drivers",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "List of drivers retrieved successfully"
            )
        ]
    )
    @GetMapping("/all")
    fun getAllDrivers(): List<DriverDto> {
        val drivers = driverService.getAll()
        return drivers.map { driverMapperDto.toDto(it) }
    }

    @Operation(
        summary = "Get driver by ID",
        description = "Retrieves driver details by ID",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Driver details retrieved successfully"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Driver not found"
            )
        ]
    )
    @GetMapping("/{id}")
    fun getDriverById(
        @Parameter(description = "ID of the driver", example = "1")
        @PathVariable id: Long
    ): DriverDto {
        val driver = driverService.getDriverById(id)
        return driverMapperDto.toDto(driver)
    }

    @Operation(
        summary = "Register a new driver",
        description = "Creates a new driver account",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Driver registered successfully"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input"
            )
        ]
    )
    @PostMapping("/register")
    fun registerDriver(
        @RequestBody registrationDto: RegistrationDriverDto
    ): DriverDto {
        val entity = driverMapperDto.fromRegistrationDto(registrationDto)
        val createdDriver = driverService.createDriver(entity)
        return driverMapperDto.toDto(createdDriver)
    }

    @Operation(
        summary = "Login driver",
        description = "Authenticates a driver and returns their details",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Driver logged in successfully"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid credentials"
            )
        ]
    )
    @PostMapping("/login")
    fun loginDriver(
        @RequestBody loginDriverDto: LoginDriverDto
    ): DriverDto {
        val driver = driverService.login(loginDriverDto.gmail, loginDriverDto.password)
        return driverMapperDto.toDto(driver)
    }

    @Operation(
        summary = "Update driver details",
        description = "Updates driver information by ID",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Driver updated successfully"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Driver not found"
            )
        ]
    )
    @PutMapping("/update/{id}")
    fun updateDriver(
        @Parameter(description = "ID of the driver", example = "1")
        @PathVariable id: Long,
        @RequestBody driverDto: DriverDto
    ): DriverDto {
        val entity = driverMapperDto.toEntity(driverDto).apply { this.id = id }
        val updatedDriver = driverService.updateDriver(entity)
        return driverMapperDto.toDto(updatedDriver)
    }

    @Operation(
        summary = "Delete driver",
        description = "Deletes a driver by credentials",
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Driver deleted successfully"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Driver not found"
            )
        ]
    )
    @DeleteMapping("/delete")
    fun deleteDriver(
        @RequestBody loginDriverDto: LoginDriverDto
    ) {
        driverService.deleteDriver(loginDriverDto.gmail, loginDriverDto.password)
    }
}