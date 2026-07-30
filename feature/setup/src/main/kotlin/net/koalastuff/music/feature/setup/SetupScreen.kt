package net.koalastuff.music.feature.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.koalastuff.music.core.data.SetupCredentialKind

@Composable
fun SetupRoute(onCompleted: () -> Unit, viewModel: SetupViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.completed) {
        if (state.completed) onCompleted()
    }
    SetupScreen(
        state = state,
        onUrlChange = viewModel::setUrl,
        onDisplayNameChange = viewModel::setDisplayName,
        onUsernameChange = viewModel::setUsername,
        onSecretChange = viewModel::setSecret,
        onCredentialKindChange = viewModel::setCredentialKind,
        onAllowHttpChange = viewModel::setAllowHttp,
        onSubmit = viewModel::submit,
        onConfirmHttp = viewModel::confirmHttpAndConnect,
        onDismissHttp = viewModel::dismissHttpConfirmation
    )
}

@Composable
private fun SetupScreen(
    state: SetupUiState,
    onUrlChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onSecretChange: (String) -> Unit,
    onCredentialKindChange: (SetupCredentialKind) -> Unit,
    onAllowHttpChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onConfirmHttp: () -> Unit,
    onDismissHttp: () -> Unit
) {
    if (state.showHttpConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissHttp,
            title = { Text(stringResource(R.string.insecure_http_title)) },
            text = { Text(stringResource(R.string.insecure_http_message)) },
            confirmButton = {
                TextButton(onClick = onConfirmHttp) {
                    Text(stringResource(R.string.connect_insecurely))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissHttp) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.setup_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = state.serverUrl,
            onValueChange = onUrlChange,
            label = { Text(stringResource(R.string.server_url)) },
            supportingText = { Text(stringResource(R.string.server_url_hint)) },
            singleLine = true,
            enabled = !state.connecting,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.displayName,
            onValueChange = onDisplayNameChange,
            label = { Text(stringResource(R.string.display_name)) },
            singleLine = true,
            enabled = !state.connecting,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.credentialKind == SetupCredentialKind.PASSWORD,
                onClick = { onCredentialKindChange(SetupCredentialKind.PASSWORD) },
                label = { Text(stringResource(R.string.password)) },
                enabled = !state.connecting
            )
            FilterChip(
                selected = state.credentialKind == SetupCredentialKind.API_KEY,
                onClick = { onCredentialKindChange(SetupCredentialKind.API_KEY) },
                label = { Text(stringResource(R.string.api_key)) },
                enabled = !state.connecting
            )
        }
        if (state.credentialKind == SetupCredentialKind.PASSWORD) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.username,
                onValueChange = onUsernameChange,
                label = { Text(stringResource(R.string.username)) },
                singleLine = true,
                enabled = !state.connecting,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.secret,
            onValueChange = onSecretChange,
            label = {
                Text(
                    stringResource(
                        if (state.credentialKind ==
                            SetupCredentialKind.API_KEY
                        ) {
                            R.string.api_key
                        } else {
                            R.string.password
                        }
                    )
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !state.connecting,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.allow_http))
                Text(
                    stringResource(R.string.allow_http_supporting),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Switch(
                checked = state.allowInsecureHttp,
                onCheckedChange = onAllowHttpChange,
                enabled = !state.connecting
            )
        }
        state.errorCode?.let { code ->
            Spacer(Modifier.height(16.dp))
            Text(
                text = errorMessage(code),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onSubmit,
            enabled = !state.connecting &&
                state.serverUrl.isNotBlank() &&
                state.secret.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.connecting) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.connect))
            }
        }
        if (state.connecting) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.connecting_and_syncing),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun errorMessage(code: String): String = when (code) {
    "malformed_url" -> stringResource(R.string.error_malformed_url)
    "cleartext_not_allowed" -> stringResource(R.string.error_http_not_allowed)
    "authentication_failed" -> stringResource(R.string.error_authentication)
    "authentication_unsupported" -> stringResource(R.string.error_auth_unsupported)
    "dns_failure" -> stringResource(R.string.error_dns)
    "connection_timeout" -> stringResource(R.string.error_timeout)
    "connection_refused" -> stringResource(R.string.error_refused)
    "tls_failure" -> stringResource(R.string.error_tls)
    else -> stringResource(R.string.error_server, code)
}
