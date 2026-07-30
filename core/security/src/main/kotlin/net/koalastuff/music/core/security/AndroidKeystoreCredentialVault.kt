package net.koalastuff.music.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.koalastuff.music.core.common.AppError
import net.koalastuff.music.core.common.KoalaMusicException

@Singleton
class AndroidKeystoreCredentialVault @Inject constructor(@ApplicationContext context: Context) :
    CredentialVault {
    private val directory = File(context.noBackupFilesDir, "credentials")

    override suspend fun put(profileId: String, credential: StoredCredential) =
        withContext(Dispatchers.IO) {
            try {
                directory.mkdirs()
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
                cipher.updateAAD(profileId.toByteArray(Charsets.UTF_8))

                val type: Int
                val plaintext: ByteArray
                when (credential) {
                    is StoredCredential.ApiKey -> {
                        type = TYPE_API_KEY
                        plaintext = credential.value.toByteArray(Charsets.UTF_8)
                    }
                    is StoredCredential.Password -> {
                        type = TYPE_PASSWORD
                        plaintext = "${credential.username}\u0000${credential.value}"
                            .toByteArray(Charsets.UTF_8)
                    }
                }
                val encrypted = try {
                    cipher.doFinal(plaintext)
                } finally {
                    plaintext.fill(0)
                }
                val destination = credentialFile(profileId)
                val temporary = File(directory, "${destination.name}.tmp")
                DataOutputStream(FileOutputStream(temporary)).use { output ->
                    output.writeInt(MAGIC)
                    output.writeByte(FORMAT_VERSION)
                    output.writeByte(type)
                    output.writeByte(cipher.iv.size)
                    output.writeInt(encrypted.size)
                    output.write(cipher.iv)
                    output.write(encrypted)
                    output.flush()
                }
                if (!temporary.renameTo(destination)) {
                    temporary.delete()
                    throw KoalaMusicException(AppError.CredentialUnavailable)
                }
            } catch (failure: KoalaMusicException) {
                throw failure
            } catch (failure: Exception) {
                throw KoalaMusicException(AppError.CredentialUnavailable, failure)
            }
        }

    override suspend fun get(profileId: String): StoredCredential? = withContext(Dispatchers.IO) {
        val source = credentialFile(profileId)
        if (!source.exists()) return@withContext null
        try {
            val record = DataInputStream(FileInputStream(source)).use { input ->
                if (input.readInt() !=
                    MAGIC
                ) {
                    throw KoalaMusicException(AppError.CredentialUnavailable)
                }
                if (input.readUnsignedByte() != FORMAT_VERSION) {
                    throw KoalaMusicException(AppError.CredentialUnavailable)
                }
                val type = input.readUnsignedByte()
                val ivSize = input.readUnsignedByte()
                val encryptedSize = input.readInt()
                if (ivSize !in 12..16 || encryptedSize !in 16..MAX_CIPHERTEXT_BYTES) {
                    throw KoalaMusicException(AppError.CredentialUnavailable)
                }
                Triple(
                    type,
                    ByteArray(ivSize).also(input::readFully),
                    ByteArray(encryptedSize).also(input::readFully)
                )
            }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(128, record.second)
            )
            cipher.updateAAD(profileId.toByteArray(Charsets.UTF_8))
            val plaintext = cipher.doFinal(record.third)
            try {
                val decoded = plaintext.toString(Charsets.UTF_8)
                when (record.first) {
                    TYPE_API_KEY -> StoredCredential.ApiKey(decoded)
                    TYPE_PASSWORD -> {
                        val separator = decoded.indexOf('\u0000')
                        if (separator <=
                            0
                        ) {
                            throw KoalaMusicException(AppError.CredentialUnavailable)
                        }
                        StoredCredential.Password(
                            username = decoded.substring(0, separator),
                            value = decoded.substring(separator + 1)
                        )
                    }
                    else -> throw KoalaMusicException(AppError.CredentialUnavailable)
                }
            } finally {
                plaintext.fill(0)
            }
        } catch (failure: KoalaMusicException) {
            throw failure
        } catch (failure: Exception) {
            throw KoalaMusicException(AppError.CredentialUnavailable, failure)
        }
    }

    override suspend fun delete(profileId: String) = withContext(Dispatchers.IO) {
        credentialFile(profileId).delete()
        Unit
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            generateKey()
        }
    }

    private fun credentialFile(profileId: String): File {
        val safeId = profileId.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        if (safeId != profileId || safeId.isBlank()) {
            throw KoalaMusicException(AppError.CredentialUnavailable)
        }
        return File(directory, "$safeId.bin")
    }

    companion object {
        private const val KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "net.koalastuff.music.credentials.v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val MAGIC = 0x4B4D4352
        private const val FORMAT_VERSION = 1
        private const val TYPE_API_KEY = 1
        private const val TYPE_PASSWORD = 2
        private const val MAX_CIPHERTEXT_BYTES = 16 * 1024
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CredentialVaultModule {
    @Binds
    abstract fun bindCredentialVault(
        implementation: AndroidKeystoreCredentialVault
    ): CredentialVault
}
