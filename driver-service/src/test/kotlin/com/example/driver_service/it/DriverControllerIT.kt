package com.example.driver_service.it

import com.example.driver_service.constant.RedisSchema
import com.example.driver_service.exception.models.ErrorResponse
import com.example.driver_service.exception.models.ValidationErrorResponse
import com.example.driver_service.model.dto.CarResponseDto
import com.example.driver_service.model.dto.CreateCarDto
import com.example.driver_service.model.dto.DriverResponseDto
import com.example.driver_service.model.dto.LoginDriverDto
import com.example.driver_service.model.dto.RegisterDriverDto
import com.example.driver_service.model.dto.UpdateCarDto
import com.example.driver_service.model.dto.UpdateDriverDto
import com.example.driver_service.model.entity.CarEntity
import com.example.driver_service.model.entity.DriverEntity
import com.example.driver_service.model.enums.Gender
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.repository.CarRepository
import com.example.driver_service.repository.DriverRepository
import com.example.driver_service.service.DriverMatchingService
import com.example.driver_service.service.LocationService
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.patchForObject
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.geo.Point
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder

class DriverControllerIT @Autowired constructor(
    private val restTemplate: TestRestTemplate,
    private val passwordEncoder: PasswordEncoder,
    private val driverRepository: DriverRepository,
    private val carRepository: CarRepository,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val locationService: LocationService,
    private val driverMatchingService: DriverMatchingService
) : BaseIT() {

    @BeforeEach
    fun cleanTable() {
        driverRepository.deleteAll()
        carRepository.deleteAll()
    }

    @Test
    @DisplayName("Успешная регистрация нового водителя")
    fun registerDriverWhenRequestIsValidReturnsCreated() {
        // Arrange
        val registerDto = RegisterDriverDto(
            name = "John",
            password = "password123",
            email = "john@example.com",
            phoneNumber = "+375291234567",
            gender = Gender.MALE
        )

        // Act
        val response = restTemplate.postForEntity<DriverResponseDto>(
            "/api/v1/drivers/register",
            registerDto
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isNotNull
        assertThat(response.body?.email).isEqualTo(registerDto.email)
        assertThat(response.body?.name).isEqualTo(registerDto.name)

        val savedDriver = driverRepository.findByEmail(registerDto.email)
        assertThat(savedDriver).isNotNull
        assertThat(savedDriver?.phoneNumber).isEqualTo(registerDto.phoneNumber)
        assertThat(savedDriver?.gender).isEqualTo(Gender.MALE)
    }

    @Test
    @DisplayName("Ошибка регистрации: почта уже есть")
    fun registerDriverWhenEmailExistsReturnsOk() {
        // Arrange
        val driverId = UUID.randomUUID()
        val existingDriver = DriverEntity(
            id = driverId,
            name = "Old Name",
            email = "existsmail@example.com",
            password = "password",
            phoneNumber = "+375290000000",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        driverRepository.save(existingDriver)

        val registerDto = RegisterDriverDto(
            name = "New Name",
            password = "password",
            email = "existsmail@example.com",
            phoneNumber = "+375299999999",
            gender = Gender.MALE
        )

        // Act
        val errorResponse = restTemplate.postForEntity<ErrorResponse>(
            "/api/v1/drivers/register",
            registerDto
        )

        // Assert
        assertThat(errorResponse.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(errorResponse.body).isNotNull
        assertEquals("Email already registered", errorResponse.body!!.message)
        assertEquals("CONFLICT", errorResponse.body!!.code)
    }

    @Test
    @DisplayName("Ошибка регистрации: почта уже есть")
    fun registerDriverWhenPhoneExistsReturnsOk() {
        // Arrange
        val driverId = UUID.randomUUID()
        val phoneNumber = "+375290000000"
        val existingDriver = DriverEntity(
            id = driverId,
            name = "john",
            email = "mail@example.com",
            password = "password",
            phoneNumber = phoneNumber,
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        driverRepository.save(existingDriver)

        val registerDto = RegisterDriverDto(
            name = "john1",
            password = "password",
            email = "johnmail@example.com",
            phoneNumber = phoneNumber,
            gender = Gender.MALE
        )

        // Act
        val errorResponse = restTemplate.postForEntity<ErrorResponse>(
            "/api/v1/drivers/register",
            registerDto
        )

        // Assert
        assertThat(errorResponse.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(errorResponse.body).isNotNull
        assertEquals("Phone number already registered", errorResponse.body!!.message)
        assertEquals("CONFLICT", errorResponse.body!!.code)
    }

    @Test
    @DisplayName("Успешный логин при верных учетных данных")
    fun loginWhenCredentialsAreCorrectReturnsOk() {
        // Arrange
        val password = "correct_password"
        val email = "john@example.com"
        val driver = DriverEntity(
            id = UUID.randomUUID(),
            name = "john",
            email = email,
            password = passwordEncoder.encode(password),
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        driverRepository.save(driver)

        val loginDto = LoginDriverDto(
            email = email,
            password = password
        )

        // Act
        val response = restTemplate.postForEntity<DriverResponseDto>(
            "/api/v1/drivers/login",
            loginDto
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.email).isEqualTo(email)
    }

    @Test
    @DisplayName("Ошибка логина при неверном пароле")
    fun loginWhenPasswordIsInvalidReturnsUnauthorized() {
        // Arrange
        val email = "user@example.com"
        val loginDto = LoginDriverDto(
            email = email,
            password = "wrong_password"
        )
        val driver = DriverEntity(
            id = UUID.randomUUID(),
            name = "john",
            email = email,
            password = passwordEncoder.encode("password"),
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        driverRepository.save(driver)

        // Act
        val response = restTemplate.postForEntity<ErrorResponse>(
            "/api/v1/drivers/login",
            loginDto
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(response.body).isNotNull
        assertThat(response.body?.code).isEqualTo("UNAUTHORIZED")
        assertEquals("Invalid email or password", response.body!!.message)
    }

    @Test
    @DisplayName("Ошибка логина при неверной почте")
    fun loginWhenEmailIsNotExistsReturnsUnauthorized() {
        // Arrange
        val loginDto = LoginDriverDto(
            email = "uuuser@example.com",
            password = "password"
        )
        val driver = DriverEntity(
            id = UUID.randomUUID(),
            name = "john",
            email = "user@example.com",
            password = "password",
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        driverRepository.save(driver)

        // Act
        val response = restTemplate.postForEntity<ErrorResponse>(
            "/api/v1/drivers/login",
            loginDto
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(response.body).isNotNull
        assertThat(response.body?.code).isEqualTo("UNAUTHORIZED")
        assertEquals("Invalid email or password", response.body!!.message)
    }

    @Test
    @DisplayName("Получение водителя по существующему ID")
    fun findByIdWhenExistsReturnsOk() {
        // Arrange
        val driverId = UUID.randomUUID()
        val driverEntity = DriverEntity(
            id = driverId,
            name = "user",
            email = "user@example.com",
            password = "password",
            phoneNumber = "+375291234567",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(4.8),
            carId = null,
        )
        driverRepository.save(driverEntity)

        // Act
        val response = restTemplate.getForEntity<DriverResponseDto>(
            "/api/v1/drivers/$driverId"
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body?.id).isEqualTo(driverId)
        assertThat(response.body?.name).isEqualTo("user")
    }

    @Test
    @DisplayName("Ошибка поиска: пользователя с таким id нет")
    fun findByIdWhenNotExistsReturnsNotFound() {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act
        val response = restTemplate.getForEntity<ErrorResponse>(
            "/api/v1/drivers/$nonExistentId"
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body).isNotNull
        assertThat(response.body?.code).isEqualTo("NOT_FOUND")
    }

    @Test
    @DisplayName("Успешное частичное обновление профиля водителя")
    fun updateDriverWhenRequestIsValidReturnsOk() {
        // Arrange
        val driverId = UUID.randomUUID()
        val john = DriverEntity(
            id = driverId,
            name = "John",
            email = "john@example.com",
            password = "password123",
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null,
        )
        driverRepository.save(john)

        val updateDto = UpdateDriverDto(
            name = "John Updated",
            phoneNumber = "+375292222222",
            gender = Gender.MALE
        )

        // Act
        val response = restTemplate.exchange<DriverResponseDto>(
            "/api/v1/drivers/$driverId",
            HttpMethod.PATCH,
            HttpEntity(updateDto)
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body?.name).isEqualTo("John Updated")
        assertThat(response.body?.phoneNumber).isEqualTo("+375292222222")

        val updatedInDb = driverRepository.findById(driverId)
        assertThat(updatedInDb?.name).isEqualTo("John Updated")
        assertThat(updatedInDb?.phoneNumber).isEqualTo("+375292222222")
    }

    @Test
    @DisplayName("Ошибка обновления водителя: номер телефона уже занят")
    fun updateDriverWhenPhoneNumberExistsReturnsConflict() {
        // Arrange
        val driverId = UUID.randomUUID()
        val otherId = UUID.randomUUID()

        val john = DriverEntity(
            id = driverId,
            name = "John",
            email = "john@example.com",
            password = "password",
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null,
        )
        val otherDriver = DriverEntity(
            id = otherId,
            name = "Other Driver",
            email = "other@example.com",
            password = "password",
            phoneNumber = "+375299999999",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(4.0),
            carId = null,
        )
        driverRepository.save(john)
        driverRepository.save(otherDriver)

        val updateDto = UpdateDriverDto(
            phoneNumber = "+375299999999",
            gender = null,
            name = null
        )

        // Act
        val response = restTemplate.exchange<ErrorResponse>(
            "/api/v1/drivers/$driverId",
            HttpMethod.PATCH,
            HttpEntity(updateDto)
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.body).isNotNull
        assertThat(response.body?.code).isEqualTo("CONFLICT")
    }

    @Test
    @DisplayName("Ошибка обновление: водителя не существует")
    fun updateDriverWhenNotFoundReturnsNotFound() {
        // Arrange
        val randomId = UUID.randomUUID()
        val updateDto = UpdateDriverDto(
            name = "John Ghost",
            phoneNumber = "+375290000000",
            gender = null
        )

        // Act
        val response = restTemplate.exchange<ErrorResponse>(
            "/api/v1/drivers/$randomId",
            HttpMethod.PATCH,
            HttpEntity(updateDto)
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body).isNotNull
        assertThat(response.body?.code).isEqualTo("NOT_FOUND")
    }

    @Test
    @DisplayName("Успешная привязка автомобиля к водителю")
    fun linkCarWhenRequestIsValidReturnsOk() {
        // Arrange
        val driverId = UUID.randomUUID()
        val driver = DriverEntity(
            id = driverId,
            name = "John",
            email = "john.car@example.com",
            password = "hashed_password",
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        driverRepository.save(driver)

        val createCarDto = CreateCarDto(
            color = "Red",
            licensePlate = "E3305AM-4",
            brand = "Tesla",
            model = "Model 3",
            seats = 5
        )

        // Act
        val response = restTemplate.postForEntity<CarResponseDto>(
            "/api/v1/drivers/$driverId/cars",
            createCarDto
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.licensePlate).isEqualTo("E3305AM-4")
        assertThat(response.body?.driverId).isEqualTo(driverId)

        val updatedJohn = driverRepository.findById(driverId)
        assertThat(updatedJohn?.carId).isEqualTo(response.body?.id)
    }

    @Test
    @DisplayName("Ошибка привязки автомобиля: номер уже существует")
    fun linkCarWhenPlateExistsReturnsConflict() {
        // Arrange
        val driverId = UUID.randomUUID()
        val plate = "1111AA-1"

        driverRepository.save(
            DriverEntity(
                id = driverId, name = "John", email = "john2@example.com",
                password = "pass", phoneNumber = "+375292222222",
                gender = Gender.MALE, rating = BigDecimal.valueOf(5.0), carId = null
            )
        )

        val firstCar = CreateCarDto("Blue", plate, "BMW", "X5", 5)
        restTemplate.postForEntity<CarResponseDto>("/api/v1/drivers/$driverId/cars", firstCar)

        val duplicateCarDto = CreateCarDto("Black", plate, "Audi", "A6", 5)

        // Act
        val response = restTemplate.postForEntity<ErrorResponse>(
            "/api/v1/drivers/$driverId/cars",
            duplicateCarDto
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.body!!.code).isEqualTo("CONFLICT")
        assertEquals("Car with license plate: $plate already exists!", response.body!!.message)
    }

    @Test
    @DisplayName("Ошибка привязки автомобиля:  водитель для привязки машины не найден")
    fun linkCarWhenDriverNotFoundReturnsNotFound() {
        // Arrange
        val randomId = UUID.randomUUID()
        val carDto = CreateCarDto("White", "2222BB-2", "Toyota", "Camry", 5)

        // Act
        val response = restTemplate.postForEntity<ErrorResponse>(
            "/api/v1/drivers/$randomId/cars",
            carDto
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.code).isEqualTo("NOT_FOUND")
    }

    @Test
    @DisplayName("Получение списка всех автомобилей водителя")
    fun getCarsByDriverIdWhenExistsReturnsList() {
        // Arrange
        val id = UUID.randomUUID()
        val driver = DriverEntity(
            id = id,
            name = "John",
            email = "john.cars@example.com",
            password = "password",
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        driverRepository.save(driver)

        val car1 = CreateCarDto(
            color = "Red",
            licensePlate = "1111AA-1",
            brand = "Tesla",
            model = "Model 3",
            seats = 5,
        )
        val car2 = CreateCarDto(
            color = "Black",
            licensePlate = "2222BB-2",
            brand = "BMW",
            model = "X5",
            seats = 5,
        )

        restTemplate.postForEntity<CarResponseDto>(
            "/api/v1/drivers/$id/cars",
            car1
        )
        restTemplate.postForEntity<CarResponseDto>(
            "/api/v1/drivers/$id/cars",
            car2
        )

        // Act
        val response = restTemplate.exchange(
            "/api/v1/drivers/$id/cars",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<CarResponseDto>>() {}
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body).hasSize(2)

        val plates = response.body?.map { it.licensePlate }
        assertThat(plates).containsExactlyInAnyOrder("1111AA-1", "2222BB-2")
    }

    @Test
    @DisplayName("Успешное назначение автомобиля основной машиной водителя")
    fun assignCarAsMainWhenValidReturnsOk() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val driver = DriverEntity(
            id = driverId,
            name = "John",
            email = "john.main@example.com",
            password = "pass",
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        val car = CarEntity(
            id = carId,
            color = "Silver",
            licensePlate = "7777AA-7",
            brand = "Mercedes",
            model = "S-Class",
            seats = 4,
            driverId = driverId,
            isDeleted = false
        )
        driverRepository.save(driver)
        carRepository.save(car)

        // Act
        val response = restTemplate.exchange<CarResponseDto>(
            "/api/v1/drivers/$driverId/cars/$carId/main",
            HttpMethod.PATCH,
            HttpEntity.EMPTY
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.id).isEqualTo(carId)

        val updatedDriver = driverRepository.findById(driverId)
        assertThat(updatedDriver?.carId).isEqualTo(carId)
    }

    @Test
    @DisplayName("Ошибка при попытке назначить основной чужую или несуществующую машину")
    fun assignCarAsMainWhenCarNotOwnedReturnsNotFound() {
        // Arrange
        val johnId = UUID.randomUUID()
        val strangerCarId = UUID.randomUUID()

        driverRepository.save(
            DriverEntity(
                id = johnId, name = "John", email = "john.fake@example.com",
                password = "pass", phoneNumber = "+375292222222",
                gender = Gender.MALE, rating = BigDecimal.valueOf(5.0), carId = null
            )
        )

        // Act
        val response = restTemplate.exchange<ErrorResponse>(
            "/api/v1/drivers/$johnId/cars/$strangerCarId/main",
            HttpMethod.PATCH,
            HttpEntity.EMPTY
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.code).isEqualTo("NOT_FOUND")
    }

    @Test
    @DisplayName("Ошибка если водитель не найден при смене основной машины")
    fun assignCarAsMainWhenDriverNotFoundReturnsNotFound() {
        // Arrange
        val fakeDriverId = UUID.randomUUID()
        val someCarId = UUID.randomUUID()

        // Act
        val response = restTemplate.patchForObject<ErrorResponse>(
            "/api/v1/drivers/$fakeDriverId/cars/$someCarId/main"
        )

        // Assert
        assertNotNull(response)
        assertEquals("NOT_FOUND", response.code)
    }

    @Test
    @DisplayName("Успешное обновление параметров автомобиля")
    fun updateCarWhenRequestIsValidReturnsOk() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val driver = DriverEntity(
            id = driverId,
            name = "John",
            email = "john.updatecar@example.com",
            password = "pass",
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        val car = CarEntity(
            id = carId,
            color = "White",
            licensePlate = "1234AB-1",
            brand = "Toyota",
            model = "Corolla",
            seats = 5,
            driverId = driverId,
            isDeleted = false
        )
        driverRepository.save(driver)
        carRepository.save(car)

        val updateCarDto = UpdateCarDto(
            color = "Black",
            brand = null,
            model = "Camry",
            seats = 4
        )

        // Act
        val response = restTemplate.patchForObject<CarResponseDto>(
            "/api/v1/drivers/$driverId/cars/$carId",
            updateCarDto
        )


        // Assert
        assertNotNull(response)
        assertThat(response.color).isEqualTo("Black")
        assertThat(response.model).isEqualTo("Camry")
        assertThat(response.brand).isEqualTo("Toyota")
        assertThat(response.seats).isEqualTo(4)

        val updatedInDb = carRepository.findByCarIdAndDriverId(carId, driverId)
        assertThat(updatedInDb!!.color).isEqualTo("Black")
        assertThat(updatedInDb.model).isEqualTo("Camry")
    }

    @Test
    @DisplayName("Ошибка при попытке обновить автомобиль, не принадлежащий водителю")
    fun updateCarWhenNotOwnedByDriverReturnsNotFound() {
        // Arrange
        val driver1Id = UUID.randomUUID()
        val driver2Id = UUID.randomUUID()
        val carOfDriver1Id = UUID.randomUUID()

        val driver1 = DriverEntity(
            id = driver1Id,
            name = "John One",
            email = "john1@example.com",
            password = "password",
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )

        val driver2 = DriverEntity(
            id = driver2Id,
            name = "John Two",
            email = "john2@example.com",
            password = "password",
            phoneNumber = "+375292222222",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )

        driverRepository.save(driver1)
        driverRepository.save(driver2)

        val carOfDriver1 = CarEntity(
            id = carOfDriver1Id,
            color = "Red",
            licensePlate = "5555XX-5",
            brand = "Ferrari",
            model = "F40",
            seats = 2,
            driverId = driver1Id,
            isDeleted = false
        )
        carRepository.save(carOfDriver1)

        driver1.carId = carOfDriver1Id
        driverRepository.update(driver1)

        val updateDto = UpdateCarDto(
            color = "Pink",
            brand = null,
            model = null,
            seats = null
        )

        // Act
        val response = restTemplate.exchange<ErrorResponse>(
            "/api/v1/drivers/$driver2Id/cars/$carOfDriver1Id",
            HttpMethod.PATCH,
            HttpEntity(updateDto)
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body).isNotNull
        assertThat(response.body?.code).isEqualTo("NOT_FOUND")
    }

    @Test
    @DisplayName("Ошибка обновления автомобиля: некорректное количестве мест")
    fun updateCarWhenSeatsInvalidReturnsBadRequest() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val driver = DriverEntity(
            id = driverId, name = "John", email = "john.valid@example.com",
            password = "pass", phoneNumber = "+375293333333",
            gender = Gender.MALE, rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        driverRepository.save(driver)

        val car = CarEntity(
            id = carId,
            color = "Blue",
            licensePlate = "4444BB-4",
            brand = "Audi",
            model = "A4",
            seats = 5,
            driverId = driverId
        )
        carRepository.save(car)

        driver.carId = carId
        driverRepository.update(driver)

        // Act
        val response = restTemplate.patchForObject<ValidationErrorResponse>(
            "/api/v1/drivers/$driverId/cars/$carId",
            UpdateCarDto(color = null, brand = null, model = null, seats = 10)
        )

        // Assert
        assertNotNull(response)
        assertThat(response.errors.size).isEqualTo(1)
        assertThat(response.code).isEqualTo("VALIDATION_FAILED")
    }

    @Test
    @DisplayName("Успешное удаление основной машины водителя")
    fun deleteCarWhenIsMainReturnsNoContent() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val driver = DriverEntity(
            id = driverId,
            name = "John",
            email = "john.delete@example.com",
            password = "pass",
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )
        driverRepository.save(driver)

        val car = CarEntity(
            id = carId,
            color = "Blue",
            licensePlate = "4444BB-4",
            brand = "Audi",
            model = "A4",
            seats = 5,
            driverId = driverId
        )
        carRepository.save(car)
        driver.carId = carId
        driverRepository.update(driver)

        // Act
        val response = restTemplate.exchange<Void>(
            "/api/v1/drivers/$driverId/cars/$carId",
            HttpMethod.DELETE,
            null
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        val deletedCar = carRepository.findById(carId)
        assertThat(deletedCar?.isDeleted).isTrue()

        val updatedJohn = driverRepository.findById(driverId)
        assertThat(updatedJohn?.carId).isNull()
    }

    @Test
    @DisplayName("Успешное удаление машины водителя, которая не является основной")
    fun deleteCarWhenIsNotMainDoesNotClearDriverCarId() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()
        val mainCarId = UUID.randomUUID()

        val driver = DriverEntity(
            id = driverId, name = "John", email = "john.multi@example.com",
            password = "pass", phoneNumber = "+375292222222",
            gender = Gender.MALE,
            carId = null
        )
        driverRepository.save(driver)

        carRepository.save(
            CarEntity(
                id = carId, color = "Black", licensePlate = "1111AA-2",
                brand = "Audi", model = "A6", seats = 5, driverId = driverId, isDeleted = false
            )
        )
        carRepository.save(
            CarEntity(
                id = mainCarId, color = "Black", licensePlate = "1111AA-1",
                brand = "Audi", model = "A6", seats = 5, driverId = driverId, isDeleted = false
            )
        )
        driver.carId = mainCarId
        driverRepository.update(driver)


        // Act
        val response = restTemplate.exchange<Void>(
            "/api/v1/drivers/$driverId/cars/$carId",
            HttpMethod.DELETE,
            null
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        assertEquals(driverRepository.findById(driverId)!!.carId, mainCarId)
        assertTrue { carRepository.findByCarIdAndDriverId(carId, driverId)!!.isDeleted }
    }

    @Test
    @DisplayName("Успешное получение автомобиля по ID для конкретного водителя")
    fun getCarWhenExistsReturnsOk() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()

        val driver = DriverEntity(
            id = driverId,
            name = "John",
            email = "john.getcar@example.com",
            password = "password",
            phoneNumber = "+375291111111",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null
        )

        val car = CarEntity(
            id = carId,
            color = "Blue",
            licensePlate = "1111AA-1",
            brand = "Tesla",
            model = "Model 3",
            seats = 5,
            driverId = driverId,
            isDeleted = false
        )

        driverRepository.save(driver)
        carRepository.save(car)
        driver.carId = carId
        driverRepository.update(driver)

        // Act
        val response = restTemplate.getForEntity<CarResponseDto>(
            "/api/v1/drivers/$driverId/cars/$carId"
        )

        // Assert
        assertNotNull(response.body)
        assertThat(response.body!!.id).isEqualTo(carId)
        assertThat(response.body!!.licensePlate).isEqualTo("1111AA-1")
        assertThat(response.body!!.driverId).isEqualTo(driverId)
    }

    @Test
    @DisplayName("Успешное изменение статуса: off_duty->available")
    fun setWorkStatus_Success() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()
        val driver = DriverEntity(
            id = driverId,
            name = "john",
            email = "john@test.com",
            password = "password",
            phoneNumber = "+375290000000",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null,
            workStatus = WorkStatus.OFF_DUTY,
        )
        val car = CarEntity(
            id = carId,
            color = "Blue",
            licensePlate = "1111AA-1",
            brand = "Tesla",
            model = "Model 3",
            seats = 5,
            driverId = driverId,
            isDeleted = false
        )
        driverRepository.save(driver)
        carRepository.save(car)
        driver.carId = carId
        driverRepository.update(driver)

        // Act
        val response = restTemplate.exchange<Void>(
            "/api/v1/drivers/$driverId/duty/start",
            HttpMethod.PATCH
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val updatedDriver = driverRepository.findById(driverId)
        assertThat(updatedDriver?.workStatus).isEqualTo(WorkStatus.AVAILABLE)
    }

    @Test
    @DisplayName("Успешное изменение статуса: available->off_duty")
    fun setWorkStatus_Success_whenDriverIsBusy() {
        // Arrange
        val driverId = UUID.randomUUID()
        val carId = UUID.randomUUID()
        val driver = DriverEntity(
            id = driverId,
            name = "john",
            email = "john@test.com",
            password = "password",
            phoneNumber = "+375290000000",
            gender = Gender.MALE,
            rating = BigDecimal.valueOf(5.0),
            carId = null,
            workStatus = WorkStatus.AVAILABLE,
        )
        val car = CarEntity(
            id = carId,
            color = "Blue",
            licensePlate = "1111AA-1",
            brand = "Tesla",
            model = "Model 3",
            seats = 5,
            driverId = driverId,
            isDeleted = false
        )
        driverRepository.save(driver)
        carRepository.save(car)
        driver.carId = carId
        driverRepository.update(driver)

        // Act
        val response = restTemplate.exchange<Void>(
            "/api/v1/drivers/$driverId/duty/stop",
            HttpMethod.PATCH
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val updatedDriver = driverRepository.findById(driverId)
        assertThat(updatedDriver?.workStatus).isEqualTo(WorkStatus.OFF_DUTY)
    }

    @Test
    @DisplayName("Ошибка изменения статуса: водитель без машины")
    fun setWorkStatus_NoCar_ReturnsBadRequest() {
        // Arrange
        val driverId = UUID.randomUUID()
        val driverWithoutCar = DriverEntity(
            id = driverId,
            name = "NoCarDriver",
            email = "nocar@test.com",
            password = "password",
            phoneNumber = "+375291112233",
            gender = Gender.OTHER,
            rating = BigDecimal.valueOf(4.0),
            carId = null,
            workStatus = WorkStatus.OFF_DUTY,
        )
        driverRepository.save(driverWithoutCar)

        // Act
        val response = restTemplate.patchForObject<ErrorResponse>(
            "/api/v1/drivers/$driverId/duty/start",
        )

        // Assert
        assertNotNull(response)
        assertEquals(response.code, "INCOMPLETE_PROFILE")
        assertEquals(driverRepository.findById(driverId)!!.workStatus, WorkStatus.OFF_DUTY)
    }

    @Test
    @DisplayName("Ошибка изменения статуса: водитель не найден")
    fun setWorkStatus_NoCar_ReturnsNotFound() {
        // Arrange
        val driverId = UUID.randomUUID()

        // Act
        val response = restTemplate.patchForObject<ErrorResponse>(
            "/api/v1/drivers/$driverId/duty/start",
        )

        // Assert
        assertNotNull(response)
        assertEquals(response.code, "NOT_FOUND")
        assertNull(driverRepository.findById(driverId))
    }

    @Test
    @DisplayName("Успешный пинг локации: данные сохраняются в Redis")
    fun pingLocation_Success() {
        // Arrange
        val id = UUID.randomUUID()
        val point = Point(53.67, 23.83)

        locationService.updateSession(id, WorkStatus.AVAILABLE)

        // Act
        val response = restTemplate.postForEntity<Void>(
            "/api/v1/drivers/$id/ping",
            point
        )

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertEquals(redisTemplate.hasKey(RedisSchema.driverStatusKey(id)), true)
        assertNotNull(redisTemplate.opsForGeo().position(RedisSchema.DRIVER_LOCATIONS_KEY, id.toString()))
    }

    @Test
    @DisplayName("Ошибка пинга: водитель не на смене (нет ключа в Redis)")
    fun pingLocation_Fail_DriverOffline() {
        // Arrange
        val id = UUID.randomUUID()
        val point = Point(53.0, 23.0)

        // Act
        val response = restTemplate.postForEntity<ErrorResponse>(
            "/api/v1/drivers/$id/ping",
            point
        )

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("NOT_FOUND", response.body?.code)
        assertNull(redisTemplate.opsForGeo().position(RedisSchema.DRIVER_LOCATIONS_KEY, id.toString())!![0])
    }

    @Test
    @DisplayName("Поиск свободного водителя")
    fun findAvailableDriver() {
        // Arrange
        val driverId = UUID.randomUUID()
        val driverId2 = UUID.randomUUID()
        val driverPoint = Point(53.2, 23.2)
        val orderPoint = Point(53.201, 23.201)

        val car = CarEntity(
            UUID.randomUUID(),
            driverId,
            "Blue",
            "3300AM-4",
            "BMW",
            "3",
            4
        )

        val driver = DriverEntity(
            id = driverId,
            name = "driver",
            email = "driver@test.com",
            password = "password",
            phoneNumber = "+375291112233",
            gender = Gender.OTHER,
            rating = BigDecimal.valueOf(5.0),
            carId = null,
            workStatus = WorkStatus.OFF_DUTY,
        )
        driverRepository.save(driver)
        carRepository.save(car)
        driver.carId = car.id
        driverRepository.update(driver)

        val car2 = CarEntity(
            UUID.randomUUID(),
            driverId2,
            "Blue",
            "3300AA-4",
            "BMW",
            "3",
            4
        )

        val driver2 = DriverEntity(
            id = driverId2,
            name = "driver2",
            email = "driver2@test.com",
            password = "passw2ord",
            phoneNumber = "+375292112233",
            gender = Gender.OTHER,
            rating = BigDecimal.valueOf(4.0),
            carId = null,
            workStatus = WorkStatus.OFF_DUTY,
        )
        driverRepository.save(driver2)
        carRepository.save(car2)
        driver2.carId = car2.id
        driverRepository.update(driver2)

        restTemplate.patchForObject<Void>("/api/v1/drivers/$driverId//duty/start")
        restTemplate.patchForObject<Void>("/api/v1/drivers/$driverId2//duty/start")

        restTemplate.postForEntity<Void>(
            "/api/v1/drivers/$driverId/ping",
            driverPoint
        )

        // Act
        val result = driverMatchingService.findBestDriver(orderPoint, 4)

        // Assert
        assertEquals(redisTemplate.opsForValue().get(RedisSchema.driverStatusKey(driverId)), WorkStatus.BUSY.toString())
        assertNotNull(result)
        assertEquals(result.email, driver.email)
        assertEquals(driverRepository.findById(driverId)!!.workStatus, WorkStatus.BUSY)
    }
}