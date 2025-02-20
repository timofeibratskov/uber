package com.example.driver_service.service

import com.example.driver_service.entity.DriverEntity
import com.example.driver_service.exception.EmailNotFoundException
import com.example.driver_service.exception.EmailAlreadyExistsException
import com.example.driver_service.exception.DriverNotFoundException
import com.example.driver_service.exception.InvalidCredentialsException
import com.example.driver_service.exception.PhoneNumberAlreadyExistsException
import com.example.driver_service.exception.NameAlreadyExistsException
import com.example.driver_service.mybatisMapper.DriverMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DriverService(
    private val driverMapper: DriverMapper
) {

    fun getAll(): List<DriverEntity> {
        return driverMapper.findAll() ?: emptyList()
    }

    fun getDriverById(id: Long): DriverEntity {
        return driverMapper.findById(id)
            ?: throw DriverNotFoundException(id)
    }

    fun login(gmail: String, password: String): DriverEntity {
        val driver = driverMapper.findByEmail(gmail)
            ?: throw EmailNotFoundException(gmail)

        if (driver.password != password) {
            throw InvalidCredentialsException()
        }

        return driver
    }

    fun createDriver(driver: DriverEntity): DriverEntity {
        if (driverMapper.findByEmail(driver.gmail) != null) {
            throw EmailAlreadyExistsException("Email ${driver.gmail} already exists")
        }
        if (driverMapper.findByPhoneNumber(driver.phoneNumber) != null) {
            throw PhoneNumberAlreadyExistsException("Phone number ${driver.phoneNumber} already exists")
        }
        if (driverMapper.findByName(driver.name) != null) {
            throw NameAlreadyExistsException("Name ${driver.name} already exists")
        }
        driverMapper.create(driver)
        return driverMapper.findById(driver.id)
            ?: throw DriverNotFoundException(driver.id)
    }

    fun updateDriver(driver: DriverEntity): DriverEntity {
        val driverByEmail = driverMapper.findByEmail(driver.gmail)
        if (driverByEmail != null && driverByEmail.id != driver.id) {
            throw EmailAlreadyExistsException("Email ${driver.gmail} already exists")
        }
        val driverByPhone = driverMapper.findByPhoneNumber(driver.phoneNumber)
        if (driverByPhone != null && driverByPhone.id != driver.id) {
            throw PhoneNumberAlreadyExistsException("Phone number ${driver.phoneNumber} already exists")
        }
        val driverByName = driverMapper.findByName(driver.name)
        if (driverByName != null && driverByName.id != driver.id) {
            throw NameAlreadyExistsException("Name ${driver.name} already exists")
        }
        driverMapper.update(driver)

        return driver
    }

    fun deleteDriver(gmail: String, password: String) {
        val driver = driverMapper.findByEmail(gmail)
            ?: throw EmailNotFoundException("Driver with email $gmail not found for delete")

        if (driver.password != password) {
            throw InvalidCredentialsException()
        }

        val deletedCount = driverMapper.delete(driver.id)
        if (deletedCount == 0) {
            throw DriverNotFoundException(driver.id)
        }
    }
}
