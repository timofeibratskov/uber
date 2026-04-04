package com.example.driver_service.unit

import com.example.driver_service.exception.CarLimitExceededException
import com.example.driver_service.exception.CarNotFoundException
import com.example.driver_service.exception.LicensePlateAlreadyExistsException
import com.example.driver_service.mapper.CarMapper
import com.example.driver_service.model.dto.CarResponseDto
import com.example.driver_service.model.dto.CreateCarDto
import com.example.driver_service.model.dto.UpdateCarDto
import com.example.driver_service.model.entity.CarEntity
import com.example.driver_service.repository.CarRepository
import com.example.driver_service.service.CarService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CarServiceTest {

    @MockK
    lateinit var carRepository: CarRepository

    @MockK
    lateinit var carMapper: CarMapper

    @InjectMockKs
    lateinit var carService: CarService

    @Test
    @DisplayName("Поиск машины: успешный возврат DTO")
    fun findByCarIdAndDriverId_Success() {
        // Arrange
        val carId = UUID.randomUUID()
        val driverId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val carEntity = CarEntity(
            id = carId,
            driverId = driverId,
            color = "Red",
            licensePlate = "7777-AA-7",
            brand = "Tesla",
            model = "Model S",
            seats = 5,
            createdAt = now,
            updatedAt = now,
            isDeleted = false
        )

        val expectedDto = CarResponseDto(
            id = carId,
            color = "Red",
            licensePlate = "7777-AA-7",
            brand = "Tesla",
            model = "Model S",
            seats = 5,
            driverId = driverId
        )

        every { carRepository.findByCarIdAndDriverId(carId, driverId) } returns carEntity
        every { carMapper.toDto(carEntity) } returns expectedDto

        // Act
        val result = carService.findByCarIdAndDriverId(carId, driverId)

        // Assert
        assertEquals(expectedDto.id, result.id)
        assertEquals(expectedDto.color, result.color)
        assertEquals(expectedDto.licensePlate, result.licensePlate)
        assertEquals(expectedDto.brand, result.brand)
        assertEquals(expectedDto.model, result.model)
        assertEquals(expectedDto.seats, result.seats)
        assertEquals(expectedDto.driverId, result.driverId)
        verify(exactly = 1) { carRepository.findByCarIdAndDriverId(carId, driverId) }
        verify(exactly = 1) { carMapper.toDto(carEntity) }
    }

    @Test
    @DisplayName("Поиск машины: выброс исключения, если машина не найдена")
    fun findByCarIdAndDriverId_NotFound() {
        // Arrange
        val carId = UUID.randomUUID()
        val driverId = UUID.randomUUID()

        every { carRepository.findByCarIdAndDriverId(carId, driverId) } returns null

        // Act
        val exception = assertThrows<CarNotFoundException> {
            carService.findByCarIdAndDriverId(carId, driverId)
        }

        // Assert
        assertEquals("Car with ID $carId not found for driver $driverId", exception.message)
        verify(exactly = 1) { carRepository.findByCarIdAndDriverId(carId, driverId) }
        verify(exactly = 0) { carMapper.toDto(any()) }
    }

    @Test
    @DisplayName("Поиск всех машин водителя: успешный возврат списка")
    fun findAllByDriverId_Success() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId1 = UUID.randomUUID()
        val carId2 = UUID.randomUUID()

        val carEntity1 = CarEntity(id = carId1, driverId = driverId, brand = "Tesla", model = "S")
        val carEntity2 = CarEntity(id = carId2, driverId = driverId, brand = "BMW", model = "M5")
        val entities = listOf(carEntity1, carEntity2)

        val dto1 = CarResponseDto(
            id = carId1,
            brand = "Tesla",
            model = "S",
            color = "",
            licensePlate = "",
            seats = 5,
            driverId = driverId
        )
        val dto2 = CarResponseDto(
            id = carId2,
            brand = "BMW",
            model = "M5",
            color = "",
            licensePlate = "",
            seats = 4,
            driverId = driverId
        )

        every { carRepository.findByDriverId(driverId) } returns entities
        every { carMapper.toDto(carEntity1) } returns dto1
        every { carMapper.toDto(carEntity2) } returns dto2

        // Act
        val result = carService.findAllByDriverId(driverId)

        // Assert
        assertEquals(2, result.size)
        assertEquals("Tesla", result[0].brand)
        assertEquals("BMW", result[1].brand)
        verify(exactly = 1) { carRepository.findByDriverId(driverId) }
        verify(exactly = 2) { carMapper.toDto(any()) }
    }

    @Test
    @DisplayName("Поиск всех машин водителя: возврат пустого списка")
    fun findAllByDriverId_EmptyList() {
        // Arrange
        val driverId = UUID.randomUUID()
        every { carRepository.findByDriverId(driverId) } returns emptyList()

        // Act
        val result = carService.findAllByDriverId(driverId)

        // Assert
        assertTrue(result.isEmpty())
        verify(exactly = 1) { carRepository.findByDriverId(driverId) }
        verify(exactly = 0) { carMapper.toDto(any()) }
    }

    @Test
    @DisplayName("Добавление машины: успешное создание новой")
    fun add_Success_NewCar() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val dto = CreateCarDto(
            color = "Black",
            licensePlate = "1111-AA-1",
            brand = "Audi",
            model = "A6",
            seats = 5
        )

        val entity = CarEntity(
            id = carId,
            driverId = driverId,
            color = "Black",
            licensePlate = "1111-AA-1",
            brand = "Audi",
            model = "A6",
            seats = 5,
            isDeleted = false
        )

        every { carRepository.findByDriverId(driverId) } returns emptyList()
        every { carRepository.findByLicensePlate(dto.licensePlate) } returns null
        every { carMapper.toEntity(driverId, dto) } returns entity
        every { carRepository.save(entity) } returns Unit

        // Act
        val result = carService.add(driverId, dto)

        // Assert
        assertEquals(carId, result.id)
        assertEquals(dto.licensePlate, result.licensePlate)
        assertEquals(driverId, result.driverId)
        verify(exactly = 1) { carRepository.findByDriverId(driverId) }
        verify(exactly = 1) { carRepository.findByLicensePlate(dto.licensePlate) }
        verify(exactly = 1) { carRepository.save(entity) }
    }

    @Test
    @DisplayName("Добавление машины: восстановление ранее удаленной")
    fun add_Success_RestoreDeleted() {
        // Arrange
        val newDriverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val dto = CreateCarDto(
            color = "White",
            licensePlate = "2222-BB-2",
            brand = "BMW",
            model = "X5",
            seats = 5
        )

        val existingDeletedCar = CarEntity(
            id = carId,
            driverId = null,
            color = "Blue",
            licensePlate = "2222-BB-2",
            brand = "BMW",
            model = "X5",
            seats = 5,
            isDeleted = true
        )

        every { carRepository.findByDriverId(newDriverId) } returns emptyList()
        every { carRepository.findByLicensePlate(dto.licensePlate) } returns existingDeletedCar
        every { carRepository.update(existingDeletedCar) } returns Unit

        // Act
        val result = carService.add(newDriverId, dto)

        // Assert
        assertFalse(result.isDeleted)
        assertEquals(newDriverId, result.driverId)
        assertEquals(carId, result.id)
        verify(exactly = 1) { carRepository.update(existingDeletedCar) }
        verify(exactly = 0) { carRepository.save(any()) }
    }

    @Test
    @DisplayName("Добавление машины: ошибка при превышении лимита")
    fun add_ThrowsException_WhenLimitExceeded() {
        // Arrange
        val driverId = UUID.randomUUID()
        val dto = CreateCarDto(
            color = "Red",
            licensePlate = "3333-CC-3",
            brand = "Opel",
            model = "Astra",
            seats = 5
        )

        val existingCars = List(3) {
            CarEntity(id = UUID.randomUUID(), driverId = driverId)
        }

        every { carRepository.findByDriverId(driverId) } returns existingCars

        // Act
        assertThrows<CarLimitExceededException> {
            carService.add(driverId, dto)
        }

        // Assert
        verify(exactly = 1) { carRepository.findByDriverId(driverId) }
        verify(exactly = 0) { carRepository.findByLicensePlate(any()) }
        verify(exactly = 0) { carRepository.save(any()) }
    }

    @Test
    @DisplayName("Добавление машины: ошибка при превышении лимита")
    fun add_ThrowsException_WhenLicensePlateAlreadyExist() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val dto = CreateCarDto(
            color = "White",
            licensePlate = "2222-BB-2",
            brand = "BMW",
            model = "X5",
            seats = 5
        )

        val entity = CarEntity(
            id = carId,
            driverId = UUID.randomUUID(),
            color = "White",
            licensePlate = "2222-BB-2",
            brand = "BMW",
            model = "X5",
            seats = 5,
            isDeleted = false
        )
        val existingDeletedCar = CarEntity(
            id = UUID.randomUUID(),
            driverId = UUID.randomUUID(),
            color = "Blue",
            licensePlate = "2112-AA-2",
            brand = "BMW",
            model = "X5",
            seats = 5,
            isDeleted = false
        )

        every { carRepository.findByDriverId(driverId) } returns emptyList()
        every { carRepository.findByLicensePlate(dto.licensePlate) } returns entity
        every { carRepository.update(existingDeletedCar) } returns Unit

        // Act
        assertThrows<LicensePlateAlreadyExistsException> {
            carService.add(driverId, dto)
        }

        // Assert
        verify(exactly = 1) { carRepository.findByDriverId(driverId) }
        verify(exactly = 1) { carRepository.findByLicensePlate(any()) }
        verify(exactly = 0) { carRepository.save(any()) }
        verify(exactly = 0) { carRepository.update(any()) }
    }

    @Test
    @DisplayName("Мягкое удаление машины: успешный вызов репозитория")
    fun softDeleteById_Success() {
        // Arrange
        val carId = UUID.randomUUID()

        every { carRepository.softDeleteById(carId) } returns 1

        // Act
        carService.softDeleteById(carId)

        // Assert
        verify(exactly = 1) { carRepository.softDeleteById(carId) }
    }

    @Test
    @DisplayName("Мягкое удаление машины: машина не найдена (0 строк обновлено)")
    fun softDeleteById_NotFound() {
        // Arrange
        val carId = UUID.randomUUID()

        every { carRepository.softDeleteById(carId) } returns 0

        // Act
        carService.softDeleteById(carId)

        // Assert
        verify(exactly = 1) { carRepository.softDeleteById(carId) }
    }

    @Test
    @DisplayName("Обновление машины: успешный сценарий")
    fun update_Success() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val updateDto = UpdateCarDto(
            color = "Red",
            brand = null,
            model = "A6",
            seats = null
        )

        val existingCar = CarEntity(
            id = carId,
            driverId = driverId,
            color = "Black",
            licensePlate = "1111-AA-1",
            brand = "Audi",
            model = "A4",
            seats = 5
        )

        val updatedCar = existingCar.copy(
            color = "Red",
            model = "A6"
        )

        val responseDto = CarResponseDto(
            id = carId,
            brand = "Audi",
            model = "A6",
            color = "Red",
            licensePlate = "1111-AA-1",
            seats = 5,
            driverId = driverId
        )

        every { carRepository.findByCarIdAndDriverId(carId, driverId) } returns existingCar
        every { carMapper.updateEntity(updateDto, existingCar) } returns updatedCar
        every { carRepository.update(updatedCar) } returns Unit
        every { carMapper.toDto(updatedCar) } returns responseDto

        // Act
        val result = carService.update(driverId, carId, updateDto)

        // Assert
        assertEquals("Red", result.color)
        assertEquals("A6", result.model)
        verify(exactly = 1) { carRepository.findByCarIdAndDriverId(carId, driverId) }
        verify(exactly = 1) { carRepository.update(any()) }
        verify(exactly = 1) { carMapper.toDto(any()) }
    }

    @Test
    @DisplayName("Обновление машины: ошибка если машина не найдена")
    fun update_ThrowsNotFound() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()
        val updateDto = UpdateCarDto(
            color = "Gold",
            brand = null,
            model = null,
            seats = null
        )

        every { carRepository.findByCarIdAndDriverId(carId, driverId) } returns null

        // Act
        assertThrows<CarNotFoundException> {
            carService.update(driverId, carId, updateDto)
        }
        // Assert
        verify(exactly = 1) { carRepository.findByCarIdAndDriverId(carId, driverId) }
        verify(exactly = 0) { carRepository.update(any()) }
        verify(exactly = 0) { carMapper.updateEntity(any(), any()) }
    }
}