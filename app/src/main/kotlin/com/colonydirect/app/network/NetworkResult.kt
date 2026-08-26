package com.colonydirect.app.network

import com.colonydirect.app.network.dto.ApiErrorResponse
import com.google.gson.Gson
import retrofit2.Response

/**
 * Thin wrapper around a network call result, keeping error-handling
 * logic out of individual screens and ViewModels.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    data object NetworkError : NetworkResult<Nothing>()
}

/**
 * Safely executes a suspend Retrofit call and maps it to [NetworkResult].
 */
suspend fun <T> safeApiCall(
    gson: Gson,
    call: suspend () -> Response<T>
): NetworkResult<T> = try {
    val response = call()
    if (response.isSuccessful) {
        val body = response.body()
        if (body != null) {
            NetworkResult.Success(body)
        } else {
            NetworkResult.Error(response.code(), "Empty response body")
        }
    } else {
        val errorBody = response.errorBody()?.string()
        val apiError = try {
            gson.fromJson(errorBody, ApiErrorResponse::class.java)
        } catch (_: Exception) {
            null
        }
        val msg = apiError?.message
            ?: apiError?.error
            ?: errorBody
            ?: "Unknown error (${response.code()})"
        NetworkResult.Error(response.code(), msg)
    }
} catch (e: java.io.IOException) {
    NetworkResult.NetworkError
} catch (e: Exception) {
    NetworkResult.Error(-1, e.message ?: "Unexpected error")
}

/** Variant for calls that return no body (e.g. 204 No Content). */
suspend fun safeApiCallNoBody(
    call: suspend () -> Response<Unit>
): NetworkResult<Unit> = try {
    val response = call()
    if (response.isSuccessful) {
        NetworkResult.Success(Unit)
    } else {
        NetworkResult.Error(response.code(), "Error ${response.code()}")
    }
} catch (e: java.io.IOException) {
    NetworkResult.NetworkError
} catch (_: Exception) {
    NetworkResult.Error(-1, "Unexpected error")
}
