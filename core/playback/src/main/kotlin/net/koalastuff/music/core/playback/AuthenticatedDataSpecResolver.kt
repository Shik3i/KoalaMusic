package net.koalastuff.music.core.playback

import android.annotation.SuppressLint
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.ResolvingDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import net.koalastuff.music.core.common.AppError
import net.koalastuff.music.core.common.KoalaMusicException
import net.koalastuff.music.core.database.ServerProfileDao
import net.koalastuff.music.core.model.ServerProfileId
import net.koalastuff.music.core.opensubsonic.AuthMaterial
import net.koalastuff.music.core.opensubsonic.OpenSubsonicClientFactory
import net.koalastuff.music.core.opensubsonic.ServerUrlPolicy
import net.koalastuff.music.core.security.CredentialVault
import net.koalastuff.music.core.security.StoredCredential

@Singleton
@OptIn(markerClass = [UnstableApi::class])
class AuthenticatedDataSpecResolver @Inject constructor(
    private val profiles: ServerProfileDao,
    private val credentials: CredentialVault,
    private val clients: OpenSubsonicClientFactory
) : ResolvingDataSource.Resolver {
    @SuppressLint("UseKtx")
    override fun resolveDataSpec(dataSpec: DataSpec): DataSpec {
        if (dataSpec.uri.scheme != OPAQUE_SCHEME) return dataSpec
        val segments = dataSpec.uri.pathSegments
        if (segments.size != 2) throw KoalaMusicException(AppError.StreamUnavailable)
        val profileId = segments[0]
        val trackId = segments[1]
        val streamUrl = runBlocking {
            val profile = profiles.byId(profileId)
                ?: throw KoalaMusicException(AppError.StreamUnavailable)
            val credential = credentials.get(profileId)
                ?: throw KoalaMusicException(AppError.CredentialUnavailable)
            val baseUrl = ServerUrlPolicy.normalize(profile.baseUrl, profile.allowInsecureHttp)
            clients.create(
                profileId = ServerProfileId(profileId),
                baseUrl = baseUrl,
                auth = credential.asAuthMaterial()
            ).streamUrl(trackId)
        }
        return dataSpec.withUri(Uri.parse(streamUrl.toString()))
    }

    private fun StoredCredential.asAuthMaterial(): AuthMaterial = when (this) {
        is StoredCredential.ApiKey -> AuthMaterial.ApiKey(value)
        is StoredCredential.Password -> AuthMaterial.Password(username, value)
    }
}
