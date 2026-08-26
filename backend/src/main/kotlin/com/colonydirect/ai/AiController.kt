package com.colonydirect.ai

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/ai")
class AiController(
    private val aiSearchService: AiSearchService
) {

    @PostMapping("/search")
    fun search(
        authentication: Authentication?,
        @Valid @RequestBody request: AiSearchRequest
    ): ResponseEntity<AiSearchResponse> {
        val userId = authentication?.name?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        val response = aiSearchService.performAiSearch(userId, request.query)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/ocr-parse")
    fun parseShoppingList(
        @Valid @RequestBody request: OcrTextRequest
    ): ResponseEntity<OcrParseResponse> {
        val response = aiSearchService.parseOcrShoppingList(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/smart-reorder")
    fun getSmartReorders(authentication: Authentication): ResponseEntity<List<SmartReorderItemResponse>> {
        val userId = UUID.fromString(authentication.name)
        val response = aiSearchService.getSmartReorders(userId)
        return ResponseEntity.ok(response)
    }
}