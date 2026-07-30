package net.koalastuff.music.core.common

object Redaction {
    private val secretQueryNames = setOf(
        "apikey",
        "p",
        "password",
        "s",
        "salt",
        "t",
        "token"
    )

    fun sanitizeUrl(raw: String): String {
        val queryStart = raw.indexOf('?')
        if (queryStart < 0) return raw
        val base = raw.substring(0, queryStart)
        val sanitized = raw.substring(queryStart + 1)
            .split('&')
            .joinToString("&") { pair ->
                val separator = pair.indexOf('=')
                if (separator < 0) return@joinToString pair
                val name = pair.substring(0, separator)
                if (name.lowercase() in secretQueryNames) "$name=REDACTED" else pair
            }
        return "$base?$sanitized"
    }

    fun sanitizeDiagnosticValue(value: String): String = value
        .replace(Regex("(?i)(apiKey|password|token|salt)\\s*[:=]\\s*[^\\s,&]+"), "$1=REDACTED")
        .let(::sanitizeUrl)
}
