package net.koalastuff.music.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RedactionTest {
    @Test
    fun `redacts every authentication query value`() {
        val result = Redaction.sanitizeUrl(
            "https://music.example/rest/ping?u=user&t=deadbeef&s=random&apiKey=secret&v=1.16.1"
        )

        assertEquals(
            "https://music.example/rest/ping?u=user&t=REDACTED&s=REDACTED&apiKey=REDACTED&v=1.16.1",
            result
        )
        assertFalse(result.contains("deadbeef"))
        assertFalse(result.contains("secret"))
    }
}
