package com.velora.vault.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class SignUpRequest(val email: String, val displayName: String)

@Serializable
data class SignInRequest(val email: String)

@Serializable
data class TokenResponse(val accessToken: String, val refreshToken: String, val expiresInSeconds: Long)

@Serializable
data class RefreshRequest(val refreshToken: String)

/**
 * Account/session endpoints only — never a place the master password or
 * vault key travel through. Real deployments would sit this behind an
 * OAuth/OIDC provider (authorization code + PKCE) rather than the simple
 * shape shown here; this interface exists to make the client/server
 * boundary explicit even though this build talks to it in a best-effort,
 * fail-soft way (see SyncRepository) so the vault stays fully usable
 * offline.
 */
interface AuthApi {
    @POST("v1/auth/sign-up")
    suspend fun signUp(@Body request: SignUpRequest): TokenResponse

    @POST("v1/auth/sign-in")
    suspend fun signIn(@Body request: SignInRequest): TokenResponse

    @POST("v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): TokenResponse

    @POST("v1/auth/sign-out-all-devices")
    suspend fun signOutAllDevices()
}
