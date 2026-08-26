package com.colonydirect.web

/** Matches Phase 9 Section 9.3 error model exactly: {"error":{"code","message","traceId"}}. */
data class ApiError(val code: String, val message: String, val traceId: String)
data class ApiErrorResponse(val error: ApiError)

/** Stable, machine-readable codes clients can branch on, per Phase 9 design note. */
class ApiException(
    val code: String,
    override val message: String,
    val httpStatus: Int
) : RuntimeException(message)

object ErrorCodes {
    const val EMAIL_ALREADY_REGISTERED = "EMAIL_ALREADY_REGISTERED"
    const val PHONE_ALREADY_REGISTERED = "PHONE_ALREADY_REGISTERED"
    const val INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
    const val ACCOUNT_INACTIVE = "ACCOUNT_INACTIVE"
    const val INVALID_REFRESH_TOKEN = "INVALID_REFRESH_TOKEN"
    const val REFRESH_TOKEN_EXPIRED = "REFRESH_TOKEN_EXPIRED"
    const val INVALID_GOOGLE_TOKEN = "INVALID_GOOGLE_TOKEN"
    const val INVALID_RESET_TOKEN = "INVALID_RESET_TOKEN"
    const val RESET_TOKEN_EXPIRED = "RESET_TOKEN_EXPIRED"
    const val USER_NOT_FOUND = "USER_NOT_FOUND"
    const val COD_NOT_ALLOWED_FOR_ORDER_TYPE = "COD_NOT_ALLOWED_FOR_ORDER_TYPE"
    const val VALIDATION_FAILED = "VALIDATION_FAILED"
}
