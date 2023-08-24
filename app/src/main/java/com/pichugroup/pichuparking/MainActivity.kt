package com.pichugroup.pichuparking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.pichugroup.pichuparking.ui.theme.PichuParkingComposeTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    private val locationPermissionWarningViewModel: LocationPermissionWarningViewModel by viewModels()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                locationPermissionWarningViewModel.triggerDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val showWarning: Boolean by locationPermissionWarningViewModel.dialogActive.collectAsState()
            PichuParkingComposeTheme {
                Column {
                    DisplayGoogleMaps()
                    if (showWarning) {
                        LocationPermissionWarning(
                            active = true,
                            onAccept = locationPermissionWarningViewModel::onDialogAccept,
                        )
                    }
                }
            }
        }
        requestLocationPermission()
    }


    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (!hasLocationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


}

@Composable
fun LocationPermissionWarning(active: Boolean, onAccept: () -> Unit) {
    if (active) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        ) {
            AlertDialog(
                onDismissRequest = onAccept,
                title = { Text("Location required for some features") },
                text = { Text("Location permissions are required for features that show your current location on the map") },
                confirmButton = {
                    TextButton(onClick = onAccept) {
                        Text("I know stfu".uppercase())
                    }
                },
            )
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun CardsPreview() {
    fun doesNothing() {}
    PichuParkingComposeTheme {
        LocationPermissionWarning(active = true, onAccept = ::doesNothing)
    }
}

@Composable
fun DisplayGoogleMaps() {
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    )
}

class LocationPermissionWarningViewModel : ViewModel() {
    private val _dialogActive = MutableStateFlow(false)
    val dialogActive: StateFlow<Boolean> = _dialogActive.asStateFlow()

    fun triggerDialog() {
        _dialogActive.value = true
    }

    fun onDialogAccept() {
        _dialogActive.value = false
    }
}