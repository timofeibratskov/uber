import com.example.rating_service.RatingServiceApplication
import com.example.rating_service.dto.RatingRequestDto
import com.example.rating_service.dto.RatingResponseDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@Testcontainers
@SpringBootTest(classes = [RatingServiceApplication::class])
@AutoConfigureMockMvc
class RatingControllerIT {

    companion object {
        @Container
        private val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:5.0.22"))
            .withCommand("--quiet", "--nojournal")
            .withStartupTimeout(Duration.ofMinutes(2))

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") {
                "mongodb://${mongoDBContainer.host}:${mongoDBContainer.firstMappedPort}/test"
            }
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper


    @Test
    fun `test create rating`() {
        mockMvc.perform(
            post("/ratings")
                .content(
                    """
                    {
                        "rideId": "ride-123",
                        "rating": 4.5,
                        "senderId": 1,
                        "recipientId": 2,
                        "senderType": "DRIVER"
                    }
                """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated)
    }

    @Test
    fun `test get ratings for user`() {
        // 1. Создаём тестовые данные через POST-запросы
        mockMvc.perform(
            post("/ratings")
                .content(
                    """
                    {
                        "rideId": "ride-1",
                        "rating": 4.0,
                        "comment": "best",
                        "senderId": 1,
                        "recipientId": 2,
                        "senderType": "DRIVER"
                    }
                """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/ratings")
                .content(
                    """
                    {
                        "rideId": "ride-2",
                        "rating": 5.0,
                        "comment": "norm",
                        "senderId": 1,
                        "recipientId": 2,
                        "senderType": "PASSENGER"
                    }
                """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated)

        // 2. Проверяем, что данные появились
        mockMvc.perform(get("/ratings/user/2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].recipientId").value(2))
            .andExpect(jsonPath("$[1].recipientId").value(2))
    }

    @Test
    fun `test get rating by id`() {
        // 1. Создаем рейтинг через POST-запрос
        mockMvc.perform(
            post("/ratings")
                .content(
                    """
                {
                    "rideId": "ride-123",
                    "rating": 4.5,
                    "comment": "good",
                    "senderId": 1,
                    "recipientId": 2,
                    "senderType": "PASSENGER"
                }
            """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated)

        // 2. Получаем ID созданного рейтинга через GET запрос всех рейтингов пользователя
        val ratingId = mockMvc.perform(get("/ratings/user/2"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it)[0]["id"].asText() }

        // 3. Проверяем получение рейтинга по ID
        mockMvc.perform(get("/ratings/$ratingId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(ratingId))
            .andExpect(jsonPath("$.rating").value(4.5))
    }

    @Test
    fun `test get user rating summary`() {
        val recipientId = 2L

        // 1. Создаем тестовые данные
        listOf(
            Triple("ride-1", 4.0f, "ok"),
            Triple("ride-2", 5.0f, "good"),
            Triple("ride-3", 3.0f, "uuwu")
        ).forEach { (rideId, rating, comment) ->
            mockMvc.perform(
                post("/ratings")
                    .content("""
                    {
                        "rideId": "$rideId",
                        "rating": $rating,
                        "comment": "$comment",
                        "senderId": 1,
                        "recipientId": $recipientId,
                        "senderType": "DRIVER"
                    }
                """.trimIndent())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated)
        }

        // 2. Проверяем статистику (используем правильные имена полей)
        mockMvc.perform(get("/ratings/user/$recipientId/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalRating").value(4.0))  // вместо averageRating
            .andExpect(jsonPath("$.quantityRating").value(3)) // вместо totalRatings
    }

    @Test
    fun `test get non-existing rating`() {
        mockMvc.perform(get("/ratings/999"))
            .andExpect(status().isNotFound)
    }

    private fun createRating(request: RatingRequestDto): RatingResponseDto {
        val result = mockMvc.perform(
            post("/ratings")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andReturn()

        return objectMapper.readValue(
            result.response.contentAsString,
            RatingResponseDto::class.java
        )
    }
}