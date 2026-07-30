package net.koalastuff.music.core.opensubsonic

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertificateException
import java.time.Instant
import javax.net.ssl.SSLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import net.koalastuff.music.core.common.AppError
import net.koalastuff.music.core.common.KoalaMusicException
import net.koalastuff.music.core.model.Album
import net.koalastuff.music.core.model.ServerCapability
import net.koalastuff.music.core.model.ServerProfileId
import net.koalastuff.music.core.model.Track
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

data class ServerIdentity(
    val apiVersion: String,
    val type: String?,
    val serverVersion: String?,
    val openSubsonic: Boolean
)

data class AlbumDetails(val album: Album, val tracks: List<Track>)

interface OpenSubsonicClient {
    suspend fun ping(): ServerIdentity
    suspend fun extensions(): List<ServerCapability>
    suspend fun newestAlbums(size: Int = 100): List<Album>
    suspend fun album(id: String): AlbumDetails
    fun streamUrl(trackId: String): HttpUrl
}

class OpenSubsonicClientFactory(private val okHttpClient: OkHttpClient) {
    fun create(
        profileId: ServerProfileId,
        baseUrl: HttpUrl,
        auth: AuthMaterial
    ): OpenSubsonicClient = DefaultOpenSubsonicClient(
        profileId = profileId,
        baseUrl = baseUrl,
        auth = auth,
        http = okHttpClient.newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    )
}

private class DefaultOpenSubsonicClient(
    private val profileId: ServerProfileId,
    private val baseUrl: HttpUrl,
    private val auth: AuthMaterial,
    private val http: OkHttpClient
) : OpenSubsonicClient {
    private val authFactory = AuthenticationQueryFactory()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    }

    override suspend fun ping(): ServerIdentity {
        val root = requestJson("ping", authenticated = true)
        return ServerIdentity(
            apiVersion =
            root.string("version") ?: throw KoalaMusicException(AppError.UnsupportedResponse),
            type = root.string("type"),
            serverVersion = root.string("serverVersion"),
            openSubsonic = root.boolean("openSubsonic") ?: false
        )
    }

    override suspend fun extensions(): List<ServerCapability> {
        val root = try {
            requestJson("getOpenSubsonicExtensions", authenticated = false)
        } catch (_: KoalaMusicException) {
            requestJson("getOpenSubsonicExtensions", authenticated = true)
        }
        val extensions = root.objectOrNull("openSubsonicExtensions")
            ?.arrayOrEmpty("openSubsonicExtension")
            .orEmpty()
        return extensions.mapNotNull { element ->
            val value = element as? JsonObject ?: return@mapNotNull null
            val name = value.string("name") ?: return@mapNotNull null
            val versions = value.arrayOrEmpty("versions")
                .mapNotNull { it.jsonPrimitive.intOrNull }
                .toSet()
            ServerCapability(name, versions)
        }
    }

    override suspend fun newestAlbums(size: Int): List<Album> {
        val root = requestJson(
            endpoint = "getAlbumList2",
            authenticated = true,
            parameters = mapOf("type" to "newest", "size" to size.coerceIn(1, 500).toString())
        )
        return root.objectOrNull("albumList2")
            ?.arrayOrEmpty("album")
            .orEmpty()
            .mapNotNull(::mapAlbum)
    }

    override suspend fun album(id: String): AlbumDetails {
        val root = requestJson(
            endpoint = "getAlbum",
            authenticated = true,
            parameters = mapOf("id" to id)
        )
        val value = root.objectOrNull("album")
            ?: throw KoalaMusicException(AppError.NotFound)
        val album = mapAlbum(value) ?: throw KoalaMusicException(AppError.UnsupportedResponse)
        val tracks = value.arrayOrEmpty("song").mapNotNull(::mapTrack)
        return AlbumDetails(album, tracks)
    }

    override fun streamUrl(trackId: String): HttpUrl =
        endpointUrl("stream", authenticated = true, parameters = mapOf("id" to trackId))

    private suspend fun requestJson(
        endpoint: String,
        authenticated: Boolean,
        parameters: Map<String, String> = emptyMap()
    ): JsonObject = withContext(Dispatchers.IO) {
        val url = endpointUrl(endpoint, authenticated, parameters)
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build()
        execute(request).use { response ->
            if (!response.isSuccessful) throw mapHttpFailure(response.code)
            val bytes = response.body.byteStream().use { it.readBounded(MAX_METADATA_BYTES) }
            val parsed = try {
                json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject
            } catch (failure: RuntimeException) {
                throw KoalaMusicException(AppError.UnsupportedResponse, failure)
            }
            val root = parsed["subsonic-response"]?.jsonObject
                ?: throw KoalaMusicException(AppError.UnsupportedResponse)
            if (root.string("status") != "ok") throw mapRemoteFailure(root)
            root
        }
    }

    private fun execute(initial: Request): Response {
        try {
            val response = http.newCall(initial).execute()
            if (response.code !in REDIRECT_CODES) return response
            val location = response.header("Location")
            val redirected = location?.let(initial.url::resolve)
            response.close()
            if (redirected == null) throw KoalaMusicException(AppError.UnsafeRedirect)
            ServerUrlPolicy.requireSafeRedirect(initial.url, redirected)
            val redirectUrl = if (redirected.querySize == 0) {
                redirected.newBuilder().encodedQuery(initial.url.encodedQuery).build()
            } else {
                redirected
            }
            return http.newCall(initial.newBuilder().url(redirectUrl).build()).execute()
        } catch (failure: KoalaMusicException) {
            throw failure
        } catch (failure: UnknownHostException) {
            throw KoalaMusicException(AppError.DnsFailure, failure)
        } catch (failure: SocketTimeoutException) {
            throw KoalaMusicException(AppError.ConnectionTimeout, failure)
        } catch (failure: ConnectException) {
            throw KoalaMusicException(AppError.ConnectionRefused, failure)
        } catch (failure: SSLException) {
            throw KoalaMusicException(AppError.TlsFailure, failure)
        } catch (failure: CertificateException) {
            throw KoalaMusicException(AppError.TlsFailure, failure)
        } catch (failure: IOException) {
            throw KoalaMusicException(AppError.ServerFailure, failure)
        }
    }

    private fun endpointUrl(
        endpoint: String,
        authenticated: Boolean,
        parameters: Map<String, String>
    ): HttpUrl = baseUrl.newBuilder()
        .addPathSegments("rest/$endpoint.view")
        .addQueryParameter("v", API_VERSION)
        .addQueryParameter("c", CLIENT_NAME)
        .addQueryParameter("f", "json")
        .apply {
            parameters.forEach(::addQueryParameter)
            if (authenticated) authFactory.parameters(auth).forEach(::addQueryParameter)
        }
        .build()

    private fun mapAlbum(element: JsonElement): Album? {
        val value = element as? JsonObject ?: return null
        val id = value.string("id") ?: return null
        val name = value.string("name") ?: return null
        return Album(
            profileId = profileId,
            remoteId = id,
            name = name,
            artistId = value.string("artistId"),
            artistName = value.string("artist"),
            coverArtId = value.string("coverArt"),
            songCount = value.int("songCount") ?: 0,
            durationSeconds = value.long("duration"),
            year = value.int("year"),
            genre = value.string("genre"),
            createdAt = value.instant("created"),
            starredAt = value.instant("starred")
        )
    }

    private fun mapTrack(element: JsonElement): Track? {
        val value = element as? JsonObject ?: return null
        val id = value.string("id") ?: return null
        val title = value.string("title") ?: return null
        return Track(
            profileId = profileId,
            remoteId = id,
            title = title,
            albumId = value.string("albumId"),
            albumName = value.string("album"),
            artistId = value.string("artistId"),
            artistName = value.string("artist"),
            trackNumber = value.int("track"),
            discNumber = value.int("discNumber"),
            durationSeconds = value.long("duration"),
            contentType = value.string("contentType"),
            suffix = value.string("suffix"),
            coverArtId = value.string("coverArt"),
            starredAt = value.instant("starred")
        )
    }

    private fun mapRemoteFailure(root: JsonObject): KoalaMusicException {
        val code = root.objectOrNull("error")?.int("code") ?: 0
        val error = when (code) {
            20, 30 -> AppError.ApiIncompatible
            40, 44 -> AppError.AuthenticationFailed
            41, 42 -> AppError.AuthenticationUnsupported
            43 -> AppError.AuthenticationFailed
            70 -> AppError.NotFound
            else -> AppError.Remote(code)
        }
        return KoalaMusicException(error)
    }

    private fun mapHttpFailure(code: Int): KoalaMusicException = KoalaMusicException(
        when (code) {
            401, 403 -> AppError.AuthenticationFailed
            404 -> AppError.NotFound
            else -> AppError.ServerFailure
        }
    )

    companion object {
        private const val API_VERSION = "1.16.1"
        private const val CLIENT_NAME = "KoalaMusic"
        private const val MAX_METADATA_BYTES = 8 * 1024 * 1024
        private val REDIRECT_CODES = setOf(301, 302, 303, 307, 308)
    }
}

private fun JsonObject.string(name: String): String? = get(name)?.jsonPrimitive?.contentOrNull

private fun JsonObject.int(name: String): Int? = get(name)?.jsonPrimitive?.intOrNull

private fun JsonObject.long(name: String): Long? = get(name)?.jsonPrimitive?.longOrNull

private fun JsonObject.boolean(name: String): Boolean? = get(name)?.jsonPrimitive?.booleanOrNull

private fun JsonObject.instant(name: String): Instant? =
    string(name)?.let { value -> runCatching { Instant.parse(value) }.getOrNull() }

private fun JsonObject.objectOrNull(name: String): JsonObject? = get(name) as? JsonObject

private fun JsonObject.arrayOrEmpty(name: String): JsonArray =
    get(name) as? JsonArray ?: JsonArray(emptyList())

private fun InputStream.readBounded(maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0
    while (true) {
        val count = read(buffer)
        if (count < 0) break
        total += count
        if (total > maxBytes) throw KoalaMusicException(AppError.UnsupportedResponse)
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}
