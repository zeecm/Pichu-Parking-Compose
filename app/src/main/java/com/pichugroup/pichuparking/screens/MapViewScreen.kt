package com.pichugroup.pichuparking.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.pichugroup.pichuparking.permissions.PermissionAlertDialog
import com.pichugroup.pichuparking.permissions.RationaleState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewScreen() {
    var isClicked by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Scaffold Example") },
                actions = {
                    IconButton(onClick = { isClicked = !isClicked }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Show menu filter chips"
                        )
                    }
                    if (isClicked) {
                        FilterChips()
                    }
                },
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 20.dp)
                    .clip(RoundedCornerShape(90.dp)),
                colors = centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
            )
        },
        content = {
            Box {
                MapsContent()
            }
        },
    )
}


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapsContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fineLocationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
        ),
    )
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var rationaleState by remember {
        mutableStateOf<RationaleState?>(null)
    }
    var currentLatLon by remember { mutableStateOf(LatLng(1.35, 103.87)) }
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLon, 15f)
    }
    Box {
        DisplayGoogleMaps(cameraPositionState = cameraPositionState, currentLatLon = currentLatLon)
        rationaleState?.run { PermissionAlertDialog(rationaleState = this) }
        FloatingActionButton(
            onClick = {
                if (fineLocationPermissionState.allPermissionsGranted) {
                    scope.launch(Dispatchers.IO) {
                        val priority = Priority.PRIORITY_HIGH_ACCURACY
                        val result = locationClient.getCurrentLocation(
                            priority,
                            CancellationTokenSource().token,
                        ).await()
                        result?.let { fetchedLocation ->
                            currentLatLon = LatLng(
                                fetchedLocation.latitude, fetchedLocation.longitude
                            )
                        }
                    }
                    cameraPositionState.move(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(
                                currentLatLon, 15f
                            )
                        )
                    )
                } else {
                    if (fineLocationPermissionState.shouldShowRationale) {
                        rationaleState = RationaleState(
                            "Request Precise Location",
                            "In order to use this feature please grant access by accepting " + "the location permission dialog." + "\n\nWould you like to continue?",
                        ) { proceed ->
                            if (proceed) {
                                fineLocationPermissionState.launchMultiplePermissionRequest()
                            }
                            rationaleState = null
                        }
                    } else {
                        fineLocationPermissionState.launchMultiplePermissionRequest()
                    }
                    Toast.makeText(context, "Location Permissions not Enabled.", Toast.LENGTH_SHORT)
                        .show()
                }
            }, modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
        ) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Current Location")
        }
    }
}

@Composable
fun DisplayGoogleMaps(
    cameraPositionState: CameraPositionState, currentLatLon: LatLng = LatLng(1.35, 103.87)
) {
    val uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = false)) }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
    ) {
        Marker(
            state = MarkerState(position = currentLatLon), title = "My Current Location"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips() {
    HorizontalGrid(
        spacing = 7.dp, modifier = Modifier.padding(7.dp)
    ) {
        var selected by remember { mutableStateOf(false) }
        var selected2 by remember { mutableStateOf(false) }
        FilterChip(
            onClick = { selected = !selected },
            label = {
                Text("Filter chip")
            },
            selected = selected,
            leadingIcon = if (selected) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Done icon",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else {
                null
            },
        )

        FilterChip(
            onClick = { selected2 = !selected2 },
            label = {
                Text("Filter chip 2")
            },
            selected = selected2,
            leadingIcon = if (selected2) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Done icon",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else {
                null
            },
        )
    }
}

@Composable
fun HorizontalGrid(
    modifier: Modifier = Modifier, spacing: Dp, content: @Composable () -> Unit
) {
    Layout(
        content = content, modifier = modifier
    ) { measurables, constraints ->
        var currentRow = 0
        var currentOrigin = IntOffset.Zero
        val spacingValue = spacing.toPx().toInt()
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints)

            if (currentOrigin.x > 0f && currentOrigin.x + placeable.width > constraints.maxWidth) {
                currentRow += 1
                currentOrigin =
                    currentOrigin.copy(x = 0, y = currentOrigin.y + placeable.height + spacingValue)
            }

            placeable to currentOrigin.also {
                currentOrigin = it.copy(x = it.x + placeable.width + spacingValue)
            }
        }

        layout(width = constraints.maxWidth,
            height = placeables.lastOrNull()?.run { first.height + second.y } ?: 0) {
            placeables.forEach {
                val (placeable, origin) = it
                placeable.place(origin.x, origin.y)
            }
        }
    }
}

@Composable
fun ShowToastMessage(context: Context, message: String, toastLength: Int) {
    Toast.makeText(context, message, toastLength).show()
}

@Composable
fun CraneDrawer(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .padding(start = 24.dp, top = 48.dp)
    ) {

    }
}