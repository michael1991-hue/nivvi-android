package app.nivvi.ui

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestBluetoothPermissions(onGranted: () -> Unit = {}) {
    val permissions = buildList {
        add(Manifest.permission.BLUETOOTH_SCAN)
        add(Manifest.permission.BLUETOOTH_CONNECT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    val state = rememberMultiplePermissionsState(permissions)
    LaunchedEffect(state.allPermissionsGranted) {
        if (state.allPermissionsGranted) onGranted()
    }
    if (!state.allPermissionsGranted) {
        LaunchedEffect(Unit) {
            state.launchMultiplePermissionRequest()
        }
    }
}
