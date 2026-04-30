package com.example.driver_service.model.entity

import com.example.driver_service.model.enums.EventType
import java.time.Instant

data class OutboxEventEntity(
    var id: Long? = null,
    var topic: String? = "",
    var eventType: EventType?,
    var payload: String? = "",
    var createdAt: Instant? = Instant.now(),
)