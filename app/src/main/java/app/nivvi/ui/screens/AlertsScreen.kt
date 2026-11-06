package app.nivvi.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.nivvi.ui.MonitorViewModel

@Composable
fun AlertsScreen(viewModel: MonitorViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Alerts", style = MaterialTheme.typography.headlineMedium)

        Text(
            text = "Low means strictly below. High means strictly above. The value must stay outside the limit for the dwell time. Gaps over ten seconds reset the timer.",
            style = MaterialTheme.typography.bodyMedium
        )

        SettingRow(
            label = "High alert",
            enabled = settings.highEnabled,
            onEnabledChange = { viewModel.updateSettings(settings.copy(highEnabled = it)) }
        )
        if (settings.highEnabled) {
            LimitSlider(
                label = "High limit",
                value = settings.highLimit,
                range = 100f..220f,
                onChange = { viewModel.updateSettings(settings.copy(highLimit = it.toInt())) }
            )
        }

        SettingRow(
            label = "Low alert",
            enabled = settings.lowEnabled,
            onEnabledChange = { viewModel.updateSettings(settings.copy(lowEnabled = it)) }
        )
        if (settings.lowEnabled) {
            LimitSlider(
                label = "Low limit",
                value = settings.lowLimit,
                range = 30f..100f,
                onChange = { viewModel.updateSettings(settings.copy(lowLimit = it.toInt())) }
            )
        }

        Text(text = "Dwell time: ${settings.dwellSeconds} seconds", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = settings.dwellSeconds.toFloat(),
            onValueChange = { viewModel.updateSettings(settings.copy(dwellSeconds = it.toInt())) },
            valueRange = 5f..120f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This is not a medical device. Alerts need a fresh reading, notification permission and a phone that can sound.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SettingRow(label: String, enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
    }
}

@Composable
private fun LimitSlider(label: String, value: Int, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Text(text = "$label: $value bpm", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = value.toFloat(),
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
