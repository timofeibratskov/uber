package com.example.driver_service.service

import com.example.driver_service.entity.CarEntity
import com.example.driver_service.exception.CarNotFoundException
import com.example.driver_service.exception.LicensePlateAlreadyExistsException
import com.example.driver_service.mybatisMapper.CarMapper
import com.example.driver_service.mybatisMapper.DriverMapper

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CarService(
    private val carMyBatisMapper: CarMapper
) {

    fun getAll(): List<CarEntity> {
        return carMyBatisMapper.findAll() ?: emptyList()
    }

    fun getCarById(id: Long): CarEntity {
        return carMyBatisMapper.findById(id)
            ?: throw CarNotFoundException(id)
    }

    fun createCar(car: CarEntity): CarEntity {
        val existingCar = carMyBatisMapper.findByLicensePlate(car.licensePlate)
        if (existingCar != null) {
            throw LicensePlateAlreadyExistsException(car.licensePlate)
        }

        carMyBatisMapper.create(car)

        return carMyBatisMapper.findById(car.id)
            ?: throw CarNotFoundException(car.id)
    }

    fun updateCar(updatedCar: CarEntity): CarEntity {
        carMyBatisMapper.findById(updatedCar.id)
            ?: throw CarNotFoundException(updatedCar.id)

        val carWithSameLicense = carMyBatisMapper.findByLicensePlate(updatedCar.licensePlate)
        if (carWithSameLicense != null && carWithSameLicense.id != updatedCar.id) {
            throw LicensePlateAlreadyExistsException("Car with license plate ${updatedCar.licensePlate} already exists")
        }

        carMyBatisMapper.update(updatedCar)

        return updatedCar
    }

    fun deleteCar(id: Long) {
        val deletedCount = carMyBatisMapper.delete(id)
        if (deletedCount == 0) {
            throw CarNotFoundException(id)
        }
    }
}
