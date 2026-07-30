package net.koalastuff.music.feature.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.koalastuff.music.core.common.KoalaMusicException
import net.koalastuff.music.core.data.MusicRepository
import net.koalastuff.music.core.data.ServerSetupRequest
import net.koalastuff.music.core.data.SetupCredentialKind

data class SetupUiState(
    val serverUrl: String = "",
    val displayName: String = "",
    val username: String = "",
    val secret: String = "",
    val credentialKind: SetupCredentialKind = SetupCredentialKind.PASSWORD,
    val allowInsecureHttp: Boolean = false,
    val showHttpConfirmation: Boolean = false,
    val connecting: Boolean = false,
    val stage: String? = null,
    val errorCode: String? = null,
    val completed: Boolean = false
)

@HiltViewModel
class SetupViewModel @Inject constructor(private val repository: MusicRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SetupUiState())
    val state: StateFlow<SetupUiState> = mutableState.asStateFlow()

    fun setUrl(value: String) = update { copy(serverUrl = value, errorCode = null) }
    fun setDisplayName(value: String) = update { copy(displayName = value) }
    fun setUsername(value: String) = update { copy(username = value) }
    fun setSecret(value: String) = update { copy(secret = value, errorCode = null) }
    fun setCredentialKind(value: SetupCredentialKind) = update {
        copy(
            credentialKind = value,
            username = if (value ==
                SetupCredentialKind.API_KEY
            ) {
                ""
            } else {
                username
            }
        )
    }
    fun setAllowHttp(value: Boolean) = update { copy(allowInsecureHttp = value) }

    fun submit() {
        val current = mutableState.value
        if (current.serverUrl.trim().startsWith("http://", ignoreCase = true) &&
            current.allowInsecureHttp &&
            !current.showHttpConfirmation
        ) {
            update { copy(showHttpConfirmation = true) }
            return
        }
        connect()
    }

    fun dismissHttpConfirmation() = update { copy(showHttpConfirmation = false) }

    fun confirmHttpAndConnect() {
        update { copy(showHttpConfirmation = false) }
        connect()
    }

    private fun connect() {
        val current = mutableState.value
        if (current.connecting) return
        viewModelScope.launch {
            update { copy(connecting = true, stage = "connecting", errorCode = null) }
            try {
                repository.connectAndSync(
                    ServerSetupRequest(
                        serverUrl = current.serverUrl,
                        displayName = current.displayName,
                        username = current.username,
                        secret = current.secret,
                        credentialKind = current.credentialKind,
                        allowInsecureHttp = current.allowInsecureHttp
                    )
                )
                update {
                    copy(connecting = false, secret = "", completed = true, stage = "complete")
                }
            } catch (failure: KoalaMusicException) {
                update {
                    copy(
                        connecting = false,
                        secret = "",
                        errorCode = failure.error.code,
                        stage = "failed"
                    )
                }
            }
        }
    }

    private inline fun update(transform: SetupUiState.() -> SetupUiState) {
        mutableState.update(transform)
    }
}
