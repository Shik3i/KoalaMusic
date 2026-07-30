package net.koalastuff.music.core.opensubsonic

import java.security.MessageDigest
import java.security.SecureRandom

sealed interface AuthMaterial {
    data class ApiKey(val value: String) : AuthMaterial
    data class Password(val username: String, val value: String) : AuthMaterial
}

internal class AuthenticationQueryFactory(private val secureRandom: SecureRandom = SecureRandom()) {
    fun parameters(auth: AuthMaterial): Map<String, String> = when (auth) {
        is AuthMaterial.ApiKey -> mapOf("apiKey" to auth.value)
        is AuthMaterial.Password -> {
            val bytes = ByteArray(12).also(secureRandom::nextBytes)
            val salt = bytes.joinToString("") { "%02x".format(it) }
            val digest = MessageDigest.getInstance("MD5")
                .digest((auth.value + salt).toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
            mapOf(
                "u" to auth.username,
                "t" to digest,
                "s" to salt
            )
        }
    }
}
