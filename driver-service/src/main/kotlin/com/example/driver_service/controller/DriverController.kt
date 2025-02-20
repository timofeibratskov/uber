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

@RestController
@RequestMapping("/api/drivers")
class DriverController(
    private val driverService: DriverService,
    private val driverMapperDto: DriverMapper
) {

    @GetMapping("/all")
    fun getAllDrivers(): List<DriverDto> {
        val drivers = driverService.getAll()
        return drivers.map { driverMapperDto.toDto(it) }
    }

    @GetMapping("/{id}")
    fun getDriverById(@PathVariable id: Long): DriverDto {
        val driver = driverService.getDriverById(id)
        return driverMapperDto.toDto(driver)
    }

    @PostMapping("/register")
    fun registerDriver(@RequestBody registrationDto: RegistrationDriverDto): DriverDto {
        val entity = driverMapperDto.fromRegistrationDto(registrationDto)
        val createdDriver = driverService.createDriver(entity)
        return driverMapperDto.toDto(createdDriver)
    }

    @PostMapping("/login")
    fun loginDriver(@RequestBody loginDriverDto: LoginDriverDto): DriverDto {
        val driver = driverService.login(loginDriverDto.gmail, loginDriverDto.password)
        return driverMapperDto.toDto(driver)
    }

    @PutMapping("/update/{id}")
    fun updateDriver(@PathVariable id: Long, @RequestBody driverDto: DriverDto): DriverDto {
        val entity = driverMapperDto.toEntity(driverDto).apply { this.id = id }
        val updatedDriver = driverService.updateDriver(entity)
        return driverMapperDto.toDto(updatedDriver)
    }

    @DeleteMapping("/delete")
    fun deleteDriver(@RequestBody loginDriverDto: LoginDriverDto) {
        driverService.deleteDriver(loginDriverDto.gmail, loginDriverDto.password)
    }
}
