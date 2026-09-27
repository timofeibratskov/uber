package com.example.driver_service.service

import com.example.driver_service.exception.DriverNotFoundException
import com.example.driver_service.exception.OpenedShiftAlreadyExistsException
import com.example.driver_service.exception.ShiftNotFoundException
import com.example.driver_service.mapper.CarMapper
import com.example.driver_service.mapper.ShiftMapper
import com.example.driver_service.model.dto.ShiftDto
import com.example.driver_service.model.entity.CarEntity
import com.example.driver_service.model.entity.ShiftEntity
import com.example.driver_service.model.enums.ShiftStatus
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.model.event.RideCompletedEvent
import com.example.driver_service.repository.ShiftRepository
import java.time.LocalDateTime
import java.util.UUID
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShiftService(
    private val driverService: DriverService,
    private val shiftRepository: ShiftRepository,
    private val shiftMapper: ShiftMapper,
    private val carMapper: CarMapper,
    private val carService: CarService

) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    fun start(driverId: UUID) {
        log.info("Starting shift ")
        driverService.setWorkStatus(driverId, WorkStatus.AVAILABLE)
        val openedShift = shiftRepository.findOpened(driverId)
        if (openedShift != null) {
            throw OpenedShiftAlreadyExistsException("Driver $driverId already has an active shift")
        } else {
            val driver = driverService.findEntityById(driverId)
                ?: throw DriverNotFoundException("Driver $driverId not found")
            val shift = ShiftEntity(
                driverId = driver.id,
                carId = driver.carId!!,
            )
            shiftRepository.save(shift)
            log.info("Shift ${shift.id} saved at ${LocalDateTime.now()}")
        }
    }

    @Transactional
    fun stop(driverId: UUID): ShiftDto {
        val openedShift = shiftRepository.findOpened(driverId)
            ?: throw ShiftNotFoundException("no have opened Shift")

        driverService.setWorkStatus(driverId, WorkStatus.OFF_SHIFT)

        openedShift.endAt = LocalDateTime.now()
        openedShift.status = ShiftStatus.CLOSED

        shiftRepository.update(openedShift)
        val carDto = carMapper.toShortDto(carService.findEntityByCarId(openedShift.carId))
        return shiftMapper.toDto(openedShift, carDto)
    }

    @Transactional(readOnly = true)
    fun getActual(driverId: UUID): ShiftDto? {
        val openedShift = shiftRepository.findOpened(driverId)
            ?: return null
        val carDto = carMapper.toShortDto(carService.findEntityByCarId(openedShift.carId))
        return shiftMapper.toDto(openedShift, carDto)
    }

    @Transactional(readOnly = true)
    fun findHistoryByDriverId(driverId: UUID): List<ShiftDto> {
        val shifts = shiftRepository.findAllByDriverId(driverId)
        val carsMap = carService.getCarsMap(shifts.map { shift -> shift.carId }.toSet())

        return shifts.map { shift ->
            val carDto = carMapper.toShortDto(carsMap[shift.carId] ?: CarEntity.emptyCar())
            shiftMapper.toDto(shift, carDto)
        }
    }

    @Transactional
    fun update(event: RideCompletedEvent) {
        val openedShift = shiftRepository.findOpened(event.driverId)
            ?: throw ShiftNotFoundException("No open shift found for driver ${event.driverId}")

        openedShift.totalRides += 1
        openedShift.totalEarnings += event.amount
        openedShift.totalDistanceMeters += (event.distanceKm * 1000).toLong()
        shiftRepository.update(openedShift)

        driverService.setWorkStatus(event.driverId, WorkStatus.AVAILABLE)
    }
}