package net.koalastuff.music.core.opensubsonic

import net.koalastuff.music.core.common.AppError
import net.koalastuff.music.core.common.KoalaMusicException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object ServerUrlPolicy {
    fun normalize(raw: String, allowInsecureHttp: Boolean): HttpUrl {
        val candidate = raw.trim()
        if (candidate.isEmpty()) throw KoalaMusicException(AppError.MalformedUrl)
        val withScheme = if ("://" in candidate) candidate else "https://$candidate"
        val parsed = withScheme.toHttpUrlOrNull()
            ?: throw KoalaMusicException(AppError.MalformedUrl)
        if (parsed.scheme != "https" && parsed.scheme != "http") {
            throw KoalaMusicException(AppError.UnsupportedScheme)
        }
        if (parsed.scheme == "http" && !allowInsecureHttp) {
            throw KoalaMusicException(AppError.CleartextNotAllowed)
        }
        if (parsed.host.isBlank()) throw KoalaMusicException(AppError.MalformedUrl)

        return parsed.newBuilder()
            .query(null)
            .fragment(null)
            .encodedPath(
                parsed.encodedPath
                    .replace(Regex("/+"), "/")
                    .let { if (it.endsWith('/')) it else "$it/" }
            )
            .build()
    }

    fun requireSafeRedirect(from: HttpUrl, to: HttpUrl) {
        val crossHost = from.host != to.host || from.port != to.port
        val downgrade = from.isHttps && !to.isHttps
        if (crossHost || downgrade) throw KoalaMusicException(AppError.UnsafeRedirect)
    }
}
