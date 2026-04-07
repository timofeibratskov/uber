package com.example.driver_service.service

import com.example.driver_service.exception.DriverIncompleteProfileException
import com.example.driver_service.exception.DriverNotFoundException
import com.example.driver_service.exception.EmailAlreadyExistsException
import com.example.driver_service.exception.InvalidCredentialsException
import com.example.driver_service.exception.InvalidStatusTransitionException
import com.example.driver_service.exception.PhoneNumberAlreadyExistsException
import com.example.driver_service.mapper.CarMapper
import com.example.driver_service.mapper.DriverMapper
import com.example.driver_service.model.dto.CarResponseDto
import com.example.driver_service.model.dto.CreateCarDto
import com.example.driver_service.model.dto.DriverResponseDto
import com.example.driver_service.model.dto.LoginDriverDto
import com.example.driver_service.model.dto.RegisterDriverDto
import com.example.driver_service.model.dto.UpdateDriverDto
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.model.view.DriverView
import com.example.driver_service.repository.DriverRepository
import java.util.UUID
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DriverService(
    private val driverMapper: DriverMapper,
    private val carMapper: CarMapper,
    private val driverRepository: DriverRepository,
    private val passwordEncoder: PasswordEncoder,
    private val carService: CarService,
    private val locationService: LocationService,
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    fun register(dto: RegisterDriverDto): DriverResponseDto {
        log.info { "Registering new driver with email: ${dto.email}" }

        if (driverRepository.existsByEmail(dto.email) == 1) {
            log.warn { "Registration failed: email ${dto.email} already exists" }
            throw EmailAlreadyExistsException("Email already registered")
        }
        if (driverRepository.existsByPhoneNumber(dto.phoneNumber) == 1) {
            log.warn { "Registration failed: phone ${dto.phoneNumber} already exists" }
            throw PhoneNumberAlreadyExistsException("Phone number already registered")
        }

        val driver = driverMapper.toEntity(dto)
        driver.password = passwordEncoder.encode(dto.password)

        driverRepository.save(driver)
        log.info { "Driver registered successfully with ID: ${driver.id}" }
        return driverMapper.toDto(driver)
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): DriverResponseDto {
        log.info { "Fetching driver profile for ID: $id" }
        val driver = driverRepository.findById(id)
            ?: throw DriverNotFoundException("Driver not found with ID: $id").also {
                log.error { "Fetch failed: ${it.message}" }
            }
        return driverMapper.toDto(driver)
    }

    @Transactional(readOnly = true)
    fun login(dto: LoginDriverDto): DriverResponseDto {
        log.info { "Login attempt for email: ${dto.email}" }
        val driver = driverRepository.findByEmail(dto.email)
            ?: throw InvalidCredentialsException("Invalid email or password").also {
                log.warn { "Login failed: driver with email ${dto.email} not found" }
            }

        if (passwordEncoder.matches(dto.password, driver.password)) {
            log.info { "Login successful for driver: ${driver.email}" }
            return driverMapper.toDto(driver)
        } else {
            log.warn { "Login failed: incorrect password for email ${dto.email}" }
            throw InvalidCredentialsException("Invalid email or password")
        }
    }

    @Transactional
    fun update(id: UUID, dto: UpdateDriverDto): DriverResponseDto {
        log.info { "Updating driver profile for ID: $id" }
        val driver = driverRepository.findById(id)
            ?: throw DriverNotFoundException("Driver not found with ID: $id").also {
                log.error { "Update failed: ${it.message}" }
            }

        if (dto.phoneNumber != null && dto.phoneNumber != driver.phoneNumber) {
            if (driverRepository.existsByPhoneNumber(dto.phoneNumber) == 1) {
                log.warn { "Update failed: phone ${dto.phoneNumber} already in use" }
                throw PhoneNumberAlreadyExistsException("Phone number already exists")
            }
            driver.phoneNumber = dto.phoneNumber
        }

        dto.name?.let { driver.name = it }
        dto.gender?.let { driver.gender = it }

        driverRepository.update(driver)
        log.info { "Successfully updated driver: $id" }
        return driverMapper.toDto(driver)
    }

    @Transactional
    fun linkCar(driverId: UUID, createCarDto: CreateCarDto): CarResponseDto {
        log.info { "Linking new car to driver: $driverId" }
        val driver = driverRepository.findById(driverId)
            ?: throw DriverNotFoundException("Driver not found").also {
                log.error { "Link car failed: ${it.message}" }
            }

        val car = carService.add(driverId, createCarDto)
        driver.carId = car.id
        driverRepository.update(driver)

        log.info { "Car ${car.id} successfully linked to driver $driverId" }
        return carMapper.toDto(car)
    }

    @Transactional
    fun unlinkCar(driverId: UUID, carId: UUID) {
        log.info { "Unlinking car $carId from driver $driverId" }
        val driver = driverRepository.findById(driverId)
            ?: throw DriverNotFoundException("Driver not found").also {
                log.error { "Unlink failed: ${it.message}" }
            }

        carService.softDeleteById(carId)

        if (driver.carId == carId) {
            log.info { "Car $carId was active for driver $driverId. Clearing current car ID." }
            driver.carId = null
            driverRepository.update(driver)
        }
    }

    @Transactional
    fun assignCarAsMain(driverId: UUID, carId: UUID): CarResponseDto {
        log.info { "Assigning car $carId as main for driver $driverId" }
        val driver = driverRepository.findById(driverId)
            ?: throw DriverNotFoundException("Driver not found")

        val mainCar = carService.findByCarIdAndDriverId(carId, driverId)

        if (driver.carId != mainCar.id) {
            driver.carId = mainCar.id
            driverRepository.update(driver)
            log.info { "Driver $driverId now has car ${mainCar.id} as main" }
        }
        return mainCar
    }

    @Transactional
    fun setWorkStatus(id: UUID, status: WorkStatus) {
        val driver = driverRepository.findById(id)
            ?: throw DriverNotFoundException("Driver not found").also {
                log.error { "Driver not found with ID: $id" }
            }
        if (driver.workStatus == status) {
            log.info { "driver with id: $id already has status: ${driver.workStatus}, skipping update" }
            return
        }
        when (status) {
            WorkStatus.AVAILABLE -> {
                if (driver.carId == null)
                    throw DriverIncompleteProfileException("Driver must have an assigned car to start duty").also {
                        log.error { "driver with id: $id must have an assigned car to start duty" }
                    }
                if (driver.workStatus == WorkStatus.OFF_DUTY)
                    locationService.updateSession(id, status)
            }

            WorkStatus.BUSY -> {
                if (driver.workStatus == WorkStatus.OFF_DUTY)
                    throw InvalidStatusTransitionException("driver cannot go BUSY from OFF_DUTY. Start duty first").also {
                        log.error { "driver with id: $id cannot go BUSY from OFF_DUTY. Start duty first" }
                    }
                locationService.updateSession(id, status)
            }

            WorkStatus.OFF_DUTY -> {
                if (driver.workStatus == WorkStatus.BUSY)
                    log.warn { "driver with $id is trying to go OFF_DUTY while having an active ride" }
                locationService.deleteSession(id)
            }
        }
        driver.workStatus = status
        driverRepository.update(driver)
    }

    @Transactional(readOnly = true)
    fun findAllAvailableDrivers(ids: List<UUID>, seats: Int): List<DriverView> {
        return driverRepository.findAvailableDrivers(ids, seats)
    }
}