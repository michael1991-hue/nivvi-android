package app.nivvi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.nivvi.ui.MonitorViewModel
import app.nivvi.ui.theme.Coral
import app.nivvi.ui.theme.Mint

@Composable
fun LiveScreen(viewModel: MonitorViewModel = viewModel()) {
    val state by viewModel.live.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Live",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.connectionState,
            style = MaterialTheme.typography.bodyMedium,
            color = Mint
        )
        Spacer(modifier = Modifier.height(40.dp))

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 48.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.heartRate?.toString() ?: "--",
                    fontSize = 88.sp,
                    fontWeight = FontWeight.Light,
                    color = Coral,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = "bpm",
                    style = MaterialTheme.typography.titleMedium,
                    color = Mint
                )
                if (state.oxygen != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "${state.oxygen}%",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "SpO₂",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = if (state.heartRate == null) "Waiting for a reading" else "Live from the band",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "This is not a medical device.\nIt does not diagnose or treat anything.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
