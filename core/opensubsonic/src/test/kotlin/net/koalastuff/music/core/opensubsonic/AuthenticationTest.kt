package net.koalastuff.music.core.opensubsonic

import java.security.SecureRandom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthenticationTest {
    @Test
    fun `api key never includes a username`() {
        val parameters = AuthenticationQueryFactory().parameters(AuthMaterial.ApiKey("key"))
        assertEquals(mapOf("apiKey" to "key"), parameters)
    }

    @Test
    fun `password becomes token and random salt without plaintext`() {
        val random = SecureRandom.getInstance("SHA1PRNG").apply { setSeed(42L) }
        val parameters = AuthenticationQueryFactory(random)
            .parameters(AuthMaterial.Password("koala", "secret"))

        assertEquals("koala", parameters["u"])
        assertTrue(parameters["s"]!!.length >= 12)
        assertEquals(32, parameters["t"]!!.length)
        assertFalse(parameters.values.contains("secret"))
    }
}
