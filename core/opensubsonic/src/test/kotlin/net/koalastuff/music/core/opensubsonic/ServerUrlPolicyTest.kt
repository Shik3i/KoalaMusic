package net.koalastuff.music.core.opensubsonic

import net.koalastuff.music.core.common.AppError
import net.koalastuff.music.core.common.KoalaMusicException
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ServerUrlPolicyTest {
    @Test
    fun `adds https and retains a server path prefix`() {
        val result = ServerUrlPolicy.normalize("music.example/navidrome", false)
        assertEquals("https://music.example/navidrome/", result.toString())
    }

    @Test
    fun `http requires exact explicit consent`() {
        val failure = assertThrows(KoalaMusicException::class.java) {
            ServerUrlPolicy.normalize("http://192.168.1.4:4533", false)
        }
        assertEquals(AppError.CleartextNotAllowed, failure.error)
    }

    @Test
    fun `redirect rejects cross host and https downgrade`() {
        assertThrows(KoalaMusicException::class.java) {
            ServerUrlPolicy.requireSafeRedirect(
                "https://music.example/rest/ping".toHttpUrl(),
                "https://attacker.example/rest/ping".toHttpUrl()
            )
        }
        assertThrows(KoalaMusicException::class.java) {
            ServerUrlPolicy.requireSafeRedirect(
                "https://music.example/rest/ping".toHttpUrl(),
                "http://music.example/rest/ping".toHttpUrl()
            )
        }
    }
}
