package net.koalastuff.music.core.security

sealed interface StoredCredential {
    data class ApiKey(val value: String) : StoredCredential
    data class Password(val username: String, val value: String) : StoredCredential
}

interface CredentialVault {
    suspend fun put(profileId: String, credential: StoredCredential)
    suspend fun get(profileId: String): StoredCredential?
    suspend fun delete(profileId: String)
}
