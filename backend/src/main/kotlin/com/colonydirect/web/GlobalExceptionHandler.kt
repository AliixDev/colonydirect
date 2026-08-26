package com.colonydirect.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.UUID

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ApiErrorResponse> {
        val traceId = UUID.randomUUID().toString()
        log.warn("API error [{}] code={} message={}", traceId, ex.code, ex.message)
        return ResponseEntity.status(ex.httpStatus)
            .body(ApiErrorResponse(ApiError(ex.code, ex.message, traceId)))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val traceId = UUID.randomUUID().toString()
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
            .ifBlank { "Validation failed" }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiErrorResponse(ApiError(ErrorCodes.VALIDATION_FAILED, message, traceId)))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ApiErrorResponse> {
        val traceId = UUID.randomUUID().toString()
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse(ApiError(ErrorCodes.INVALID_CREDENTIALS, "Invalid email or password.", traceId)))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiErrorResponse> {
        val traceId = UUID.randomUUID().toString()
        log.error("Unhandled exception [{}]", traceId, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse(ApiError("INTERNAL_ERROR", "An unexpected error occurred.", traceId)))
    }
}
