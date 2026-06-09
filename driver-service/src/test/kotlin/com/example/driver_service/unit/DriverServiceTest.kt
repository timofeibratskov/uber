package com.example.driver_service.unit

import com.example.driver_service.client.RatingServiceClient
import com.example.driver_service.constant.RedisSchema
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
import com.example.driver_service.model.dto.DriverRatingResponse
import com.example.driver_service.model.dto.DriverResponseDto
import com.example.driver_service.model.dto.LoginDriverDto
import com.example.driver_service.model.dto.RegisterDriverDto
import com.example.driver_service.model.dto.UpdateDriverDto
import com.example.driver_service.model.entity.CarEntity
import com.example.driver_service.model.entity.DriverEntity
import com.example.driver_service.model.enums.Gender
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.model.view.CarView
import com.example.driver_service.model.view.DriverView
import com.example.driver_service.repository.DriverRepository
import com.example.driver_service.service.CarService
import com.example.driver_service.service.DriverService
import com.example.driver_service.service.LocationService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockKExtension::class)
class DriverServiceTest {

    @MockK
    lateinit var driverRepository: DriverRepository

    @MockK
    lateinit var driverMapper: DriverMapper

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @MockK
    lateinit var carMapper: CarMapper

    @MockK
    lateinit var locationService: LocationService

    @MockK
    lateinit var carService: CarService

    @MockK
    lateinit var ratingServiceClient: RatingServiceClient

    @MockK
    lateinit var driverCache: StringRedisTemplate

    @MockK
    lateinit var objectMapper: ObjectMapper

    @MockK
    lateinit var valueOperations: ValueOperations<String, String>

    @InjectMockKs
    lateinit var driverService: DriverService

    @Test
    @DisplayName("Регистрация водителя: успешный сценарий")
    fun register_Success() {
        // Arrange
        val dto = RegisterDriverDto(
            name = "Тимофей",
            email = "tim@example.com",
            password = "raw_password",
            phoneNumber = "+375291234567",
            gender = Gender.MALE
        )

        val driverId = UUID.randomUUID()
        val encodedPassword = "encoded_password"

        val driverEntity = DriverEntity(
            id = driverId,
            name = "Тимофей",
            email = "tim@example.com",
            password = "raw_password",
            phoneNumber = "+375291234567",
            gender = Gender.MALE,
            carId = null
        )

        every { driverRepository.existsByEmail(dto.email) } returns 0
        every { driverRepository.existsByPhoneNumber(dto.phoneNumber) } returns 0
        every { driverMapper.toEntity(dto) } returns driverEntity
        every { passwordEncoder.encode(dto.password) } returns encodedPassword
        every { driverRepository.save(any()) } returns 1

        // Act
        val result = driverService.register(dto)

        // Assert
        assertEquals("Hi, ${driverEntity.name}, you are registered successfully!", result)

        assertEquals(encodedPassword, driverEntity.password)
        verify(exactly = 1) { driverRepository.save(driverEntity) }
        verify(exactly = 1) { passwordEncoder.encode("raw_password") }
    }

    @Test
    @DisplayName("Регистрация водителя: ошибка при существующем email")
    fun register_ThrowsEmailAlreadyExists() {
        // Arrange
        val dto = RegisterDriverDto(
            name = "Test",
            email = "exists@example.com",
            password = "password",
            phoneNumber = "+375291111111",
            gender = Gender.OTHER
        )

        every { driverRepository.existsByEmail(dto.email) } returns 1

        // Act
        val exception = assertThrows<EmailAlreadyExistsException> {
            driverService.register(dto)
        }

        // Assert
        assertEquals("Email already registered", exception.message)
        verify(exactly = 1) { driverRepository.existsByEmail(dto.email) }
        verify(exactly = 0) { driverRepository.save(any()) }
    }

    @Test
    @DisplayName("Регистрация водителя: ошибка при существующем номере телефона")
    fun register_ThrowsPhoneNumberAlreadyExists() {
        // Arrange
        val dto = RegisterDriverDto(
            name = "Test",
            email = "new@example.com",
            password = "password",
            phoneNumber = "+375290000000",
            gender = Gender.OTHER
        )

        every { driverRepository.existsByEmail(dto.email) } returns 0
        every { driverRepository.existsByPhoneNumber(dto.phoneNumber) } returns 1

        // Act
        val exception = assertThrows<PhoneNumberAlreadyExistsException> {
            driverService.register(dto)
        }

        // Assert
        assertEquals("Phone number already registered", exception.message)
        verify(exactly = 1) { driverRepository.existsByPhoneNumber(dto.phoneNumber) }
        verify(exactly = 0) { driverRepository.save(any()) }
    }

    @Test
    @DisplayName("Успешное получение данных из кэша Redis (без похода в БД и Feign)")
    fun findById_ReturnsDriverFromCache_Successfully() {
        // Arrange
        val driverId = UUID.randomUUID()

        val carId = UUID.randomUUID()

        val cacheKey = RedisSchema.driverCacheKey(driverId)

        val jsonString = """{"id":"$driverId",
            |"name":"Timofei",
            |"email":"tim@example.com",
            |"phoneNumber":"+375291112233",
            |"rating":4.8,"gender":"MALE",
            |"carId":"$carId"}"""
            .trimMargin()

        val expectedDto = DriverResponseDto(
            id = driverId,
            name = "Timofei",
            email = "tim@example.com",
            phoneNumber = "+375291112233",
            rating = BigDecimal.valueOf(4.8),
            gender = Gender.MALE,
            carId = carId
        )

        every { driverCache.opsForValue() } returns valueOperations

        every { valueOperations.get(cacheKey) } returns jsonString
        every { objectMapper.readValue(jsonString, DriverResponseDto::class.java) } returns expectedDto

        // Act
        val result = driverService.findById(driverId)

        // Assert
        assertNotNull(result)
        assertEquals(expectedDto.id, result.id)
        assertEquals(expectedDto.name, result.name)
        assertEquals(expectedDto.email, result.email)
        assertEquals(expectedDto.phoneNumber, result.phoneNumber)
        assertEquals(expectedDto.rating, result.rating)
        assertEquals(expectedDto.gender, result.gender)
        assertEquals(expectedDto.carId, result.carId)

        verify(exactly = 1) { driverCache.opsForValue() }
        verify(exactly = 1) { valueOperations.get(cacheKey) }
        verify(exactly = 1) { objectMapper.readValue(jsonString, DriverResponseDto::class.java) }

        verify(exactly = 0) { driverRepository.findById(any()) }
        verify(exactly = 0) { ratingServiceClient.getUserRating(any()) }
        verify(exactly = 0) { driverMapper.toDto(any(), any()) }
        verify(exactly = 0) { valueOperations.set(any(), any(), any(), any()) }

    }


    @Test
    @DisplayName("В кэше пусто, успешное получение из БД и Feign с последующей записью в Redis")
    fun findById_FetchesFromDbAndFeign_ThenCachesAndReturns() {
        // Arrange
        val driverId = UUID.randomUUID()

        val carId = UUID.randomUUID()

        val cacheKey = RedisSchema.driverCacheKey(driverId)

        val jsonString = "{\"id\":\"$driverId\"}"

        val rating = BigDecimal.valueOf(4.8)

        val driverEntity = DriverEntity(
            id = driverId,
            name = "Timofei",
            email = "tim@example.com",
            password = "encoded_password",
            phoneNumber = "+375291112233",
            gender = Gender.MALE,
            carId = carId,
            workStatus = WorkStatus.OFF_DUTY
        )

        val responseDto = DriverResponseDto(
            id = driverId,
            name = "Timofei",
            email = "tim@example.com",
            phoneNumber = "+375291112233",
            rating = rating,
            gender = Gender.MALE,
            carId = carId
        )

        every { driverCache.opsForValue() } returns valueOperations

        every { valueOperations.get(cacheKey) } returns null

        every { driverRepository.findById(driverId) } returns driverEntity

        val feignResponse = mockk<ResponseEntity<DriverRatingResponse>>()

        every { feignResponse.body } returns DriverRatingResponse(rating = rating)
        every { ratingServiceClient.getUserRating(driverId) } returns feignResponse

        every { driverMapper.toDto(driverEntity, rating) } returns responseDto
        every { objectMapper.writeValueAsString(responseDto) } returns jsonString
        every { valueOperations.set(cacheKey, jsonString, 40, TimeUnit.MINUTES) } just runs

        // Act
        val result = driverService.findById(driverId)

        // Assert
        assertNotNull(result)
        assertEquals(driverId, result.id)
        assertEquals("Timofei", result.name)
        assertEquals(rating, result.rating)
        assertEquals(Gender.MALE, result.gender)
        assertEquals(carId, result.carId)

        verify(exactly = 1) { valueOperations.get(cacheKey) }
        verify(exactly = 1) { driverRepository.findById(driverId) }
        verify(exactly = 1) { ratingServiceClient.getUserRating(driverId) }
        verify(exactly = 1) { driverMapper.toDto(driverEntity, rating) }
        verify(exactly = 1) { objectMapper.writeValueAsString(responseDto) }
        verify(exactly = 1) { valueOperations.set(cacheKey, jsonString, 40, TimeUnit.MINUTES) }

        verify(exactly = 0) { objectMapper.readValue(any<String>(), DriverResponseDto::class.java) }
    }

    @Test
    @DisplayName("Поиск водителя по ID: ошибка если не найден")
    fun findById_ThrowsNotFound() {
        // Arrange
        val driverId = UUID.randomUUID()
        val cacheKey = RedisSchema.driverCacheKey(driverId)

        every { driverCache.opsForValue() } returns valueOperations

        every { valueOperations.get(cacheKey) } returns null
        every { driverRepository.findById(driverId) } returns null

        // Act
        val exception = assertThrows<DriverNotFoundException> {
            driverService.findById(driverId)
        }

        // Assert
        assertEquals("Driver not found with ID: $driverId", exception.message)

        verify(exactly = 1) { valueOperations.get(cacheKey) }
        verify(exactly = 1) { driverRepository.findById(driverId) }

        verify(exactly = 0) { ratingServiceClient.getUserRating(any()) }
        verify(exactly = 0) { driverMapper.toDto(any(), any()) }
        verify(exactly = 0) { valueOperations.set(any(), any(), any(), any()) }
        verify(exactly = 0) { objectMapper.writeValueAsString(any()) }
    }

    @Test
    @DisplayName("Вход в систему: успешный сценарий")
    fun login_Success() {
        // Arrange
        val loginDto = LoginDriverDto(
            email = "tim@example.com",
            password = "raw_password"
        )

        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()
        val encodedPassword = "encoded_password"

        val driverEntity = DriverEntity(
            id = driverId,
            name = "Timofei",
            email = "tim@example.com",
            password = encodedPassword,
            phoneNumber = "+375291112233",
            gender = Gender.MALE,
            carId = carId
        )


        every { driverRepository.findByEmail(loginDto.email) } returns driverEntity
        every { passwordEncoder.matches(loginDto.password, encodedPassword) } returns true

        // Act
        val result = driverService.login(loginDto)

        // Assert
        assertEquals("Hi, ${driverEntity.name}, you are with us again!", result)
        verify(exactly = 1) { driverRepository.findByEmail(loginDto.email) }
        verify(exactly = 1) { passwordEncoder.matches("raw_password", encodedPassword) }
    }

    @Test
    @DisplayName("Вход в систему: ошибка при неверном пароле")
    fun login_ThrowsInvalidCredentials_WhenPasswordIncorrect() {
        // Arrange
        val loginDto = LoginDriverDto(
            email = "tim@example.com",
            password = "wrong_password"
        )

        val driverId = UUID.randomUUID()
        val encodedPassword = "encoded_password"

        val driverEntity = DriverEntity(
            id = driverId,
            name = "Timofei",
            email = "tim@example.com",
            password = encodedPassword,
            phoneNumber = "+375291112233",
            gender = Gender.MALE,
            carId = null
        )

        every { driverRepository.findByEmail(loginDto.email) } returns driverEntity
        every { passwordEncoder.matches(loginDto.password, encodedPassword) } returns false

        // Act
        val exception = assertThrows<InvalidCredentialsException> {
            driverService.login(loginDto)
        }

        // Assert
        assertEquals("Invalid email or password", exception.message)
        verify(exactly = 1) { driverRepository.findByEmail(loginDto.email) }
        verify(exactly = 1) { passwordEncoder.matches("wrong_password", encodedPassword) }
    }

    @Test
    @DisplayName("Вход в систему: ошибка если пользователь не найден")
    fun login_ThrowsInvalidCredentials_WhenEmailNotFound() {
        // Arrange
        val loginDto = LoginDriverDto(
            email = "notfound@example.com",
            password = "any_password"
        )

        every { driverRepository.findByEmail(loginDto.email) } returns null

        // Act
        val exception = assertThrows<InvalidCredentialsException> {
            driverService.login(loginDto)
        }

        // Assert
        assertEquals("Invalid email or password", exception.message)
        verify(exactly = 1) { driverRepository.findByEmail(loginDto.email) }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
    }

    @Test
    @DisplayName("Обновление профиля: успешный сценарий")
    fun update_Success() {
        // Arrange
        val id = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val updateDto = UpdateDriverDto(
            name = "Timofei New",
            phoneNumber = "+375299998877",
            gender = Gender.MALE
        )

        val existingDriver = DriverEntity(
            id = id,
            name = "Timofei Old",
            email = "tim@example.com",
            password = "encoded_password",
            phoneNumber = "+375291112233",
            gender = Gender.OTHER,
            carId = carId
        )

        every { driverRepository.findById(id) } returns existingDriver
        every { driverRepository.existsByPhoneNumber(updateDto.phoneNumber!!) } returns 0
        every { driverRepository.update(any()) } returns Unit

        // Act
        driverService.update(id, updateDto)

        // Assert
        assertThat(existingDriver.name).isEqualTo("Timofei New")
        assertThat(existingDriver.phoneNumber).isEqualTo("+375299998877")
        assertThat(existingDriver.gender).isEqualTo(Gender.MALE)

        verify(exactly = 1) { driverRepository.findById(id) }
        verify(exactly = 1) { driverRepository.existsByPhoneNumber("+375299998877") }

        verify(exactly = 1) {
            driverRepository.update(match {
                it.id == id && it.name == "Timofei New" && it.phoneNumber == "+375299998877"
            })
        }
    }

    @Test
    @DisplayName("Обновление профиля: ошибка если телефон уже занят")
    fun update_ThrowsPhoneNumberAlreadyExists() {
        // Arrange
        val id = UUID.randomUUID()
        val updateDto = UpdateDriverDto(
            name = "New Name",
            phoneNumber = "+375290000000",
            gender = Gender.MALE
        )

        val existingDriver = DriverEntity(
            id = id,
            name = "Old Name",
            email = "tim@example.com",
            password = "encoded_password",
            phoneNumber = "+375291112233",
            gender = Gender.MALE,
            carId = null
        )

        every { driverRepository.findById(id) } returns existingDriver
        every { driverRepository.existsByPhoneNumber(updateDto.phoneNumber!!) } returns 1

        // Act
        val exception = assertThrows<PhoneNumberAlreadyExistsException> {
            driverService.update(id, updateDto)
        }

        // Assert
        assertEquals("Phone number already exists", exception.message)
        verify(exactly = 1) { driverRepository.existsByPhoneNumber("+375290000000") }
        verify(exactly = 0) { driverRepository.update(any()) }
    }

    @Test
    @DisplayName("Обновление профиля: ошибка если водитель не найден")
    fun update_ThrowsNotFound() {
        // Arrange
        val id = UUID.randomUUID()
        val updateDto = UpdateDriverDto(
            name = "Some Name",
            phoneNumber = null,
            gender = null
        )

        every { driverRepository.findById(id) } returns null

        // Act
        val exception = assertThrows<DriverNotFoundException> {
            driverService.update(id, updateDto)
        }

        // Assert
        assertEquals("Driver not found with ID: $id", exception.message)
        verify(exactly = 1) { driverRepository.findById(id) }
        verify(exactly = 0) { driverRepository.update(any()) }
    }

    @Test
    @DisplayName("Привязка машины к водителю: успешный сценарий")
    fun linkCar_Success() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val createCarDto = CreateCarDto(
            brand = "Tesla",
            model = "Model 3",
            color = "White",
            licensePlate = "7777-PE-7",
            seats = 5
        )

        val driverEntity = DriverEntity(
            id = driverId,
            name = "Timofei",
            email = "tim@example.com",
            password = "encoded_password",
            phoneNumber = "+375291112233",
            gender = Gender.MALE,
            carId = null
        )

        val carEntity = CarEntity(
            id = carId,
            driverId = driverId,
            brand = "Tesla",
            model = "Model 3",
            color = "White",
            licensePlate = "7777-PE-7",
            seats = 5,
            isDeleted = false
        )

        val responseDto = CarResponseDto(
            id = carId,
            brand = "Tesla",
            model = "Model 3",
            color = "White",
            licensePlate = "7777-PE-7",
            seats = 5,
            driverId = driverId
        )

        every { driverRepository.findById(driverId) } returns driverEntity
        every { carService.add(driverId, createCarDto) } returns carEntity
        every { driverRepository.update(any()) } returns Unit
        every { carMapper.toDto(carEntity) } returns responseDto

        // Act
        val result = driverService.linkCar(driverId, createCarDto)

        // Assert
        assertEquals(carId, result.id)
        assertEquals(carId, driverEntity.carId)
        verify(exactly = 1) { driverRepository.findById(driverId) }
        verify(exactly = 1) { carService.add(driverId, createCarDto) }
        verify(exactly = 1) { driverRepository.update(driverEntity) }
    }

    @Test
    @DisplayName("Привязка машины к водителю: ошибка если водитель не найден")
    fun linkCar_ThrowsNotFound() {
        // Arrange
        val driverId = UUID.randomUUID()
        val createCarDto = CreateCarDto(
            brand = "BMW",
            model = "M5",
            color = "Black",
            licensePlate = "1111-AA-1",
            seats = 4
        )

        every { driverRepository.findById(driverId) } returns null

        // Act
        val exception = assertThrows<DriverNotFoundException> {
            driverService.linkCar(driverId, createCarDto)
        }

        // Assert
        assertEquals("Driver not found", exception.message)
        verify(exactly = 1) { driverRepository.findById(driverId) }
        verify(exactly = 0) { carService.add(any(), any()) }
        verify(exactly = 0) { driverRepository.update(any()) }
    }

    @Test
    @DisplayName("Отвязка машины: успешное удаление активной машины")
    fun unlinkCar_Success_ActiveCar() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val driverEntity = DriverEntity(
            id = driverId,
            name = "Timofei",
            email = "tim@example.com",
            password = "encoded_password",
            phoneNumber = "+375291112233",
            gender = Gender.MALE,
            carId = carId
        )

        every { driverRepository.findById(driverId) } returns driverEntity
        every { carService.softDeleteById(carId) } returns Unit
        every { driverRepository.update(any()) } returns Unit

        // Act
        driverService.unlinkCar(driverId, carId)

        // Assert
        assertEquals(null, driverEntity.carId)
        verify(exactly = 1) { driverRepository.findById(driverId) }
        verify(exactly = 1) { carService.softDeleteById(carId) }
        verify(exactly = 1) { driverRepository.update(driverEntity) }
    }

    @Test
    @DisplayName("Отвязка машины: удаление неактивной машины (без обновления водителя)")
    fun unlinkCar_Success_NotActiveCar() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carIdForDelete = UUID.randomUUID()
        val activeCarId = UUID.randomUUID()

        val driverEntity = DriverEntity(
            id = driverId,
            name = "Timofei",
            email = "tim@example.com",
            password = "encoded_password",
            phoneNumber = "+375291112233",
            gender = Gender.MALE,
            carId = activeCarId
        )

        every { driverRepository.findById(driverId) } returns driverEntity
        every { carService.softDeleteById(carIdForDelete) } returns Unit

        // Act
        driverService.unlinkCar(driverId, carIdForDelete)

        // Assert
        assertEquals(activeCarId, driverEntity.carId)
        verify(exactly = 1) { driverRepository.findById(driverId) }
        verify(exactly = 1) { carService.softDeleteById(carIdForDelete) }
        verify(exactly = 0) { driverRepository.update(any()) }
    }

    @Test
    @DisplayName("Отвязка машины: ошибка если водитель не найден")
    fun unlinkCar_ThrowsNotFound() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        every { driverRepository.findById(driverId) } returns null

        // Act
        val exception = assertThrows<DriverNotFoundException> {
            driverService.unlinkCar(driverId, carId)
        }

        // Assert
        assertEquals("Driver not found", exception.message)
        verify(exactly = 1) { driverRepository.findById(driverId) }
        verify(exactly = 0) { carService.softDeleteById(any()) }
    }

    @Test
    @DisplayName("Назначение основной машины: успешное обновление")
    fun assignCarAsMain_Success_NewAssignment() {
        // Arrange
        val driverId = UUID.randomUUID()
        val newCarId = UUID.randomUUID()
        val oldCarId = UUID.randomUUID()

        val driverEntity = DriverEntity(
            id = driverId,
            name = "Timofei",
            email = "tim@example.com",
            password = "encoded_password",
            phoneNumber = "+375291112233",
            gender = Gender.MALE,
            carId = oldCarId
        )

        val mainCarDto = CarResponseDto(
            id = newCarId,
            brand = "Audi",
            model = "A6",
            color = "Black",
            licensePlate = "5555-IT-7",
            seats = 5,
            driverId = driverId
        )

        every { driverRepository.findById(driverId) } returns driverEntity
        every { carService.findByCarIdAndDriverId(newCarId, driverId) } returns mainCarDto
        every { driverRepository.update(any()) } returns Unit

        // Act
        val result = driverService.assignCarAsMain(driverId, newCarId)

        // Assert
        assertEquals(newCarId, result.id)
        assertEquals(newCarId, driverEntity.carId)
        verify(exactly = 1) { driverRepository.findById(driverId) }
        verify(exactly = 1) { carService.findByCarIdAndDriverId(newCarId, driverId) }
        verify(exactly = 1) { driverRepository.update(driverEntity) }
    }

    @Test
    @DisplayName("Назначение основной машины: машина уже является основной (без обновления)")
    fun assignCarAsMain_Success_AlreadyMain() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val driverEntity = DriverEntity(
            id = driverId,
            name = "Timofei",
            email = "tim@example.com",
            password = "encoded_password",
            phoneNumber = "+375291112233",
            gender = Gender.MALE,
            carId = carId
        )

        val mainCarDto = CarResponseDto(
            id = carId,
            brand = "BMW",
            model = "M5",
            color = "Blue",
            licensePlate = "1111-AA-1",
            seats = 4,
            driverId = driverId
        )

        every { driverRepository.findById(driverId) } returns driverEntity
        every { carService.findByCarIdAndDriverId(carId, driverId) } returns mainCarDto

        // Act
        val result = driverService.assignCarAsMain(driverId, carId)

        // Assert
        assertEquals(carId, result.id)
        verify(exactly = 1) { driverRepository.findById(driverId) }
        verify(exactly = 0) { driverRepository.update(any()) }
    }

    @Test
    @DisplayName("Назначение основной машины: ошибка если водитель не найден")
    fun assignCarAsMain_ThrowsNotFound() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        every { driverRepository.findById(driverId) } returns null

        // Act
        val exception = assertThrows<DriverNotFoundException> {
            driverService.assignCarAsMain(driverId, carId)
        }

        // Assert
        assertEquals("Driver not found", exception.message)
        verify(exactly = 1) { driverRepository.findById(driverId) }
        verify(exactly = 0) { carService.findByCarIdAndDriverId(any(), any()) }
    }

    @Test
    @DisplayName("Успешное изменение рабочего статуса: статус изменен на AVAILABLE")
    fun setWorkStatus_Available_Success() {
        // Arrange
        val id = UUID.randomUUID()
        val carId = UUID.randomUUID()
        val driver = DriverEntity(
            id = id,
            name = "john",
            email = "johhn@grsu.by",
            password = "secure_password_hash",
            phoneNumber = "+375291112233",
            gender = Gender.OTHER,
            carId = carId,
            workStatus = WorkStatus.OFF_DUTY,
        )

        every { locationService.updateSession(any(), any()) } returns Unit
        every { driverRepository.findById(id) } returns driver
        every { driverRepository.update(any()) } returns Unit

        // Act
        driverService.setWorkStatus(id, WorkStatus.AVAILABLE)

        // Assert
        assertEquals(WorkStatus.AVAILABLE, driver.workStatus)
        verify(exactly = 1) { driverRepository.findById(id) }
        verify(exactly = 1) { driverRepository.update(match { it.workStatus == WorkStatus.AVAILABLE }) }
        verify(exactly = 1) { locationService.updateSession(any(), any()) }
    }

    @Test
    @DisplayName("Ошибка изменения статуса: попытка стать AVAILABLE без привязанного автомобиля")
    fun setWorkStatus_Available_NoCar_ThrowsException() {
        // Arrange
        val id = UUID.randomUUID()
        val driverWithoutCar = DriverEntity(
            id = id,
            name = "Ivan",
            email = "ivan@example.com",
            password = "password123",
            phoneNumber = "+375336667788",
            gender = Gender.OTHER,
            carId = null,
            workStatus = WorkStatus.OFF_DUTY,
        )

        every { driverRepository.findById(id) } returns driverWithoutCar

        // Act
        val exception = assertThrows<DriverIncompleteProfileException> {
            driverService.setWorkStatus(id, WorkStatus.AVAILABLE)
        }
        // Assert
        assertEquals("Driver must have an assigned car to start duty", exception.message)
        verify(exactly = 0) { driverRepository.update(any()) }
    }

    @Test
    @DisplayName("Ошибка изменения статуса: переход в BUSY невозможен, если статус OFF_DUTY")
    fun setWorkStatus_BusyWhileOffDuty_ThrowsException() {
        // Arrange
        val id = UUID.randomUUID()
        val driverOffDuty = DriverEntity(
            id = id,
            name = "Dmitry",
            email = "dima@test.com",
            password = "hidden_pass",
            phoneNumber = "+375259990011",
            gender = Gender.OTHER,
            carId = UUID.randomUUID(),
            workStatus = WorkStatus.OFF_DUTY
        )

        every { driverRepository.findById(id) } returns driverOffDuty

        // Act
        val exception = assertThrows<InvalidStatusTransitionException> {
            driverService.setWorkStatus(id, WorkStatus.BUSY)
        }
        // Assert
        assertTrue(exception.message!!.contains("driver cannot go BUSY from OFF_DUTY. Start duty first"))
        verify(exactly = 0) { driverRepository.update(any()) }
    }

    @Test
    @DisplayName("Идемпотентность: пропуск обновления, если статус уже совпадает")
    fun setWorkStatus_SameStatus_NoUpdate() {
        // Arrange
        val id = UUID.randomUUID()
        val currentStatus = WorkStatus.AVAILABLE
        val driverAlreadyAvailable = DriverEntity(
            id = id,
            name = "Alex",
            email = "alex@mail.com",
            password = "hash",
            phoneNumber = "+375441234567",
            gender = Gender.OTHER,
            carId = UUID.randomUUID(),
            workStatus = currentStatus,
        )

        every { driverRepository.findById(id) } returns driverAlreadyAvailable

        // Act
        driverService.setWorkStatus(id, currentStatus)

        // Assert
        verify(exactly = 1) { driverRepository.findById(id) }
        verify(exactly = 0) { driverRepository.update(any()) }
    }

    @Test
    @DisplayName("Успешный Поиск свободных водителей: пустой список")
    fun findAllAvailableDrivers_getEmptyList() {
        // Arrange
        val ids = listOf(UUID.randomUUID())
        val seats = 4

        every { driverRepository.findAvailableDrivers(ids, seats) } returns emptyList()

        // Act
        driverService.findAllAvailableDrivers(ids, seats)

        // Assert
        verify(exactly = 1) { driverRepository.findAvailableDrivers(any(), any()) }
    }

    @Test
    @DisplayName("Успешный Поиск свободных водителей: непустой список")
    fun findAllAvailableDrivers_getDriversList() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()
        val ids = listOf(driverId)
        val seats = 4

        val availableDriver = DriverView(
            driverId,
            "john",
            "john@gmail.com",
            "+375295555555",
            Gender.MALE,
            CarView(
                carId,
                "red",
                "1122AA-1",
                "BMW",
                "3",
                4
            )
        )

        every { driverRepository.findAvailableDrivers(ids, seats) } returns listOf(availableDriver)

        // Act
        val result = driverService.findAllAvailableDrivers(ids, seats)

        // Assert
        verify(exactly = 1) { driverRepository.findAvailableDrivers(any(), any()) }
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(seats, result.first().car!!.seats)
    }
}