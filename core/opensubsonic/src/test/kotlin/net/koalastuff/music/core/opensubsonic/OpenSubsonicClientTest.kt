package net.koalastuff.music.core.opensubsonic

import kotlinx.coroutines.runBlocking
import net.koalastuff.music.core.model.ServerProfileId
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OpenSubsonicClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun startServer() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun `maps realistic Navidrome album response and tolerates unknown fields`() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "subsonic-response": {
                    "status": "ok",
                    "version": "1.16.1",
                    "album": {
                      "id": "album-1",
                      "name": "Forest Signals",
                      "artist": "Night Koala",
                      "artistId": "artist-1",
                      "songCount": 1,
                      "created": "2026-07-29T20:15:00Z",
                      "futureField": {"safe": true},
                      "song": [{
                        "id": "track-1",
                        "title": "Canopy",
                        "albumId": "album-1",
                        "artist": "Night Koala",
                        "duration": 243,
                        "unknownOptionalField": "ignored"
                      }]
                    }
                  }
                }
                """.trimIndent()
            )
        )
        val client = client(AuthMaterial.ApiKey("test-api-key"))

        val result = client.album("album-1")

        assertEquals("Forest Signals", result.album.name)
        assertEquals(1, result.album.songCount)
        assertEquals("Canopy", result.tracks.single().title)
        assertNull(result.tracks.single().contentType)
        val request = server.takeRequest()
        assertEquals("/rest/getAlbum.view", request.requestUrl!!.encodedPath)
        assertEquals("test-api-key", request.requestUrl!!.queryParameter("apiKey"))
        assertNull(request.requestUrl!!.queryParameter("u"))
    }

    @Test
    fun `discovers OpenSubsonic extensions without sending credentials`() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "subsonic-response": {
                    "status": "ok",
                    "version": "1.16.1",
                    "openSubsonicExtensions": {
                      "openSubsonicExtension": [
                        {"name": "formPost", "versions": [1]},
                        {"name": "songLyrics", "versions": [1, 2]}
                      ]
                    }
                  }
                }
                """.trimIndent()
            )
        )

        val capabilities = client(AuthMaterial.Password("koala", "secret")).extensions()

        assertEquals(setOf("formPost", "songLyrics"), capabilities.map { it.name }.toSet())
        val request = server.takeRequest()
        val requestUrl = request.requestUrl!!
        assertFalse(requestUrl.queryParameterNames.contains("u"))
        assertFalse(requestUrl.queryParameterNames.contains("t"))
        assertFalse(requestUrl.queryParameterNames.contains("s"))
        assertTrue(requestUrl.queryParameterNames.containsAll(setOf("v", "c", "f")))
    }

    private fun client(auth: AuthMaterial): OpenSubsonicClient =
        OpenSubsonicClientFactory(OkHttpClient()).create(
            profileId = ServerProfileId("profile-1"),
            baseUrl = server.url("/"),
            auth = auth
        )
}
