package com.example.driver_service.model.entity

import java.time.Instant

data class OutboxEventEntity(
    var id: Long? = null,
    var topic: String? = "",
    var eventType: String?,
    var payload: String? = "",
    var createdAt: Instant? = Instant.now(),
)