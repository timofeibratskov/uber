package com.example.driver_service.service

import com.example.driver_service.model.entity.ShiftEntity
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.repository.ShiftRepository
import java.time.LocalDateTime
import java.util.UUID
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShiftService(
    private val driverService: DriverService,
    private val shiftRepository: ShiftRepository

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
            throw RuntimeException("Driver $driverId already has an active shift")
        } else {
            val driver = driverService.findById(driverId)
            val shift = ShiftEntity(
                id = UUID.randomUUID(),
                driverId = driverId,
                carId = driver.carId!!,
            )

            log.info("Shift ${shift.id} created")
            shiftRepository.save(shift)
            log.info("Shift ${shift.id} saved at ${LocalDateTime.now()}")
        }
    }

    @Transactional
    fun stop(driverId: UUID) {
        driverService.setWorkStatus(driverId, WorkStatus.OFF_SHIFT)
    }
}