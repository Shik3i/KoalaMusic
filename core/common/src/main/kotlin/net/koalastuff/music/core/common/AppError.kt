package net.koalastuff.music.core.common

sealed interface AppError {
    val code: String

    data object MalformedUrl : AppError {
        override val code = "malformed_url"
    }
    data object UnsupportedScheme : AppError {
        override val code = "unsupported_scheme"
    }
    data object CleartextNotAllowed : AppError {
        override val code = "cleartext_not_allowed"
    }
    data object DnsFailure : AppError {
        override val code = "dns_failure"
    }
    data object ConnectionTimeout : AppError {
        override val code = "connection_timeout"
    }
    data object ConnectionRefused : AppError {
        override val code = "connection_refused"
    }
    data object TlsFailure : AppError {
        override val code = "tls_failure"
    }
    data object AuthenticationFailed : AppError {
        override val code = "authentication_failed"
    }
    data object AuthenticationUnsupported : AppError {
        override val code = "authentication_unsupported"
    }
    data object ApiIncompatible : AppError {
        override val code = "api_incompatible"
    }
    data object UnsupportedResponse : AppError {
        override val code = "unsupported_response"
    }
    data object ServerFailure : AppError {
        override val code = "server_failure"
    }
    data object StreamUnavailable : AppError {
        override val code = "stream_unavailable"
    }
    data object DatabaseFailure : AppError {
        override val code = "database_failure"
    }
    data object SyncInterrupted : AppError {
        override val code = "sync_interrupted"
    }
    data object CredentialUnavailable : AppError {
        override val code = "credential_unavailable"
    }
    data object UnsafeRedirect : AppError {
        override val code = "unsafe_redirect"
    }
    data object NotFound : AppError {
        override val code = "not_found"
    }

    data class Remote(val remoteCode: Int) : AppError {
        override val code = "remote_$remoteCode"
    }
}

class KoalaMusicException(val error: AppError, cause: Throwable? = null) :
    Exception(error.code, cause)
