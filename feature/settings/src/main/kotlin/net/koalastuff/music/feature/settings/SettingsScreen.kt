package net.koalastuff.music.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.koalastuff.music.core.model.ServerCapabilities
import net.koalastuff.music.core.model.ServerProfile

@Composable
fun SettingsScreen(
    profile: ServerProfile,
    capabilities: ServerCapabilities?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRemoveProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.settings_back)) }
        Text(
            stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge
        )
        SettingLine(stringResource(R.string.profile), profile.displayName)
        SettingLine(
            stringResource(R.string.server),
            profile.serverType ?: stringResource(R.string.unknown)
        )
        SettingLine(
            stringResource(R.string.server_version),
            profile.serverVersion ?: stringResource(R.string.unknown)
        )
        SettingLine(
            stringResource(R.string.api_version),
            profile.apiVersion ?: stringResource(R.string.unknown)
        )
        SettingLine(
            stringResource(R.string.connection),
            if (profile.allowInsecureHttp) {
                stringResource(R.string.insecure_http)
            } else {
                stringResource(R.string.https)
            }
        )
        SettingLine(
            stringResource(R.string.capabilities),
            capabilities?.values?.joinToString {
                it.name
            }?.ifBlank { stringResource(R.string.none_reported) }
                ?: stringResource(R.string.none_reported)
        )
        Text(
            stringResource(R.string.privacy_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 20.dp)
        )
        Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.refresh_library))
        }
        TextButton(onClick = onRemoveProfile, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.remove_profile), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SettingLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
    HorizontalDivider()
}
