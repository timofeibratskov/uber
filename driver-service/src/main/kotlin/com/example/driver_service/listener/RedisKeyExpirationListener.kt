package com.example.driver_service.listener

import com.example.driver_service.constant.RedisSchema
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.service.DriverService
import java.util.UUID
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component

@Component
class RedisKeyExpirationListener(
    private val driverService: DriverService
) : MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val id = UUID.fromString(message.toString().removePrefix(RedisSchema.DRIVER_STATUS_PREFIX))
        driverService.setWorkStatus(id, WorkStatus.OFF_DUTY)
    }
}