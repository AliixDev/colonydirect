package com.colonydirect.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

data class GoogleIdentity(val sub: String, val email: String?, val emailVerified: Boolean, val name: String?)

/**
 * Verifies a Google Sign-In id_token via Google's tokeninfo endpoint and checks
 * the audience matches our configured OAuth client ID. Using the tokeninfo
 * endpoint (rather than local JWKS verification) trades a small amount of
 * latency for simplicity; acceptable for this phase, revisit if login volume
 * makes the extra network hop a bottleneck.
 */
@Component
class GoogleTokenVerifier(
    @Value("\${app.google.client-id}") private val expectedClientId: String
) {
    private val restClient = RestClient.create("https://oauth2.googleapis.com")

    fun verify(idToken: String): GoogleIdentity? {
        return try {
            val response = restClient.get()
                .uri { it.path("/tokeninfo").queryParam("id_token", idToken).build() }
                .retrieve()
                .body(GoogleTokenInfo::class.java) ?: return null

            if (response.aud != expectedClientId) return null

            GoogleIdentity(
                sub = response.sub,
                email = response.email,
                emailVerified = response.email_verified?.toBoolean() ?: false,
                name = response.name
            )
        } catch (e: RestClientException) {
            null
        }
    }

    private data class GoogleTokenInfo(
        val sub: String,
        val aud: String,
        val email: String?,
        val email_verified: String?,
        val name: String?
    )
}
