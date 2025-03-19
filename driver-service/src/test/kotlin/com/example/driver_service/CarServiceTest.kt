package com.example.driver_service

import com.example.driver_service.entity.CarEntity
import com.example.driver_service.exception.CarNotFoundException
import com.example.driver_service.exception.LicensePlateAlreadyExistsException
import com.example.driver_service.mybatisMapper.CarMapper
import com.example.driver_service.service.CarService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CarServiceTest {

    @Mock
    private lateinit var carMyBatisMapper: CarMapper

    @InjectMocks
    private lateinit var carService: CarService

    private lateinit var car: CarEntity

    @BeforeEach
    fun setUp() {
        car = CarEntity(
            id = 1L,
            driverId = 1L,
            color = "Red",
            licensePlate = "ABC123",
            brand = "Toyota Corolla",
            seats = 4

        )
    }

    @Test
    fun `getAll should return list of cars`() {
        `when`(carMyBatisMapper.findAll()).thenReturn(listOf(car))

        val cars = carService.getAll()

        assertNotNull(cars)
        assertEquals(1, cars.size)
        assertEquals(car, cars[0])
    }

    @Test
    fun `getCarById should return car when found`() {
        `when`(carMyBatisMapper.findById(1L)).thenReturn(car)

        val foundCar = carService.getCarById(1L)

        assertNotNull(foundCar)
        assertEquals(car, foundCar)
    }

    @Test
    fun `getCarById should throw CarNotFoundException when not found`() {
        `when`(carMyBatisMapper.findById(1L)).thenReturn(null)

        assertThrows(CarNotFoundException::class.java) {
            carService.getCarById(1L)
        }
    }

    @Test
    fun `createCar should return created car when successful`() {
        `when`(carMyBatisMapper.findByLicensePlate("ABC123")).thenReturn(null)
        `when`(carMyBatisMapper.create(car)).thenReturn(1)
        `when`(carMyBatisMapper.findById(1L)).thenReturn(car)

        val createdCar = carService.createCar(car)

        assertNotNull(createdCar)
        assertEquals(car, createdCar)
    }

    @Test
    fun `createCar should throw LicensePlateAlreadyExistsException when license plate exists`() {
        `when`(carMyBatisMapper.findByLicensePlate("ABC123")).thenReturn(car)

        assertThrows(LicensePlateAlreadyExistsException::class.java) {
            carService.createCar(car)
        }
    }

    @Test
    fun `updateCar should return updated car when successful`() {
        `when`(carMyBatisMapper.findById(1L)).thenReturn(car)
        `when`(carMyBatisMapper.findByLicensePlate("ABC123")).thenReturn(null)
        `when`(carMyBatisMapper.update(car)).thenReturn(1)

        val updatedCar = carService.updateCar(car)

        assertNotNull(updatedCar)
        assertEquals(car, updatedCar)
    }

    @Test
    fun `updateCar should throw CarNotFoundException when car not found`() {
        `when`(carMyBatisMapper.findById(1L)).thenReturn(null)

        assertThrows(CarNotFoundException::class.java) {
            carService.updateCar(car)
        }
    }

    @Test
    fun `updateCar should throw LicensePlateAlreadyExistsException when license plate exists for another car`() {
        val existingCar = CarEntity(id = 2L, licensePlate = "ABC123", color = "red", driverId = 1L, brand = "mers", seats = 3)
        `when`(carMyBatisMapper.findById(1L)).thenReturn(car)
        `when`(carMyBatisMapper.findByLicensePlate("ABC123")).thenReturn(existingCar)

        assertThrows(LicensePlateAlreadyExistsException::class.java) {
            carService.updateCar(car)
        }
    }

    @Test
    fun `deleteCar should not throw exception when successful`() {
        `when`(carMyBatisMapper.delete(1L)).thenReturn(1)

        assertDoesNotThrow {
            carService.deleteCar(1L)
        }
    }

    @Test
    fun `deleteCar should throw CarNotFoundException when car not found`() {
        `when`(carMyBatisMapper.delete(1L)).thenReturn(0)

        assertThrows(CarNotFoundException::class.java) {
            carService.deleteCar(1L)
        }
    }
}