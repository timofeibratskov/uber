import com.example.rating_service.RatingServiceApplication
import com.github.dockerjava.api.model.HostConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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

    @Test
    fun `test create rating`() {
        mockMvc.perform(
            post("/ratings")
                .content("""
                    {
                        "rideId": "ride-123",
                        "rating": 4.5,
                        "senderId": 1,
                        "recipientId": 2,
                        "senderType": "DRIVER"
                    }
                """.trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated)
    }
}