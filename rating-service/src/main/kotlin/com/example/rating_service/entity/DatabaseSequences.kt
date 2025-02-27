package com.example.rating_service.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "database_sequences")
data class DatabaseSequences(
    @Id
    val id: String? = null,
    var seq: Long = 0
)
