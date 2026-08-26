package com.colonydirect.ai

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "search_logs")
class SearchLog(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id")
    val userId: UUID? = null,

    @Column(name = "raw_query", nullable = false, length = 500)
    val rawQuery: String,

    @Column(name = "normalized_query", nullable = false, length = 500)
    val normalizedQuery: String,

    @Column(name = "detected_language", nullable = false, length = 20)
    val detectedLanguage: String = "EN",

    @Column(name = "result_count", nullable = false)
    val resultCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)