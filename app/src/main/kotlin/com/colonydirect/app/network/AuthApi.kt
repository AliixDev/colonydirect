package com.colonydirect.app.network

import com.colonydirect.app.network.dto.AuthResponse
import com.colonydirect.app.network.dto.LoginRequest
import com.colonydirect.app.network.dto.LogoutRequest
import com.colonydirect.app.network.dto.RefreshRequest
import com.colonydirect.app.network.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("${ApiConstants.API_VERSION}/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("${ApiConstants.API_VERSION}/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("${ApiConstants.API_VERSION}/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<AuthResponse>

    @POST("${ApiConstants.API_VERSION}/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>

    @POST("${ApiConstants.API_VERSION}/auth/logout-all")
    suspend fun logoutAll(): Response<Unit>
}
