package com.pichugroup.pichuparking.screens

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.pichugroup.pichuparking.R
import com.pichugroup.pichuparking.api.PichuParkingAPIClient
import com.pichugroup.pichuparking.api.PichuParkingData
import com.pichugroup.pichuparking.api.PichuParkingLots
import com.pichugroup.pichuparking.api.PichuParkingRates
import com.pichugroup.pichuparking.permissions.PermissionAlertDialog
import com.pichugroup.pichuparking.permissions.RationaleState
import com.pichugroup.pichuparking.utils.distanceBetweenTwoCoordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewScreen() {
    MapsContent()
    MapOptionsButton()
}


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
    var currentLatLon by remember { mutableStateOf(LatLng(1.35, 103.87)) }
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLon, 15f)
    }
    var currentZoom by remember { mutableStateOf(15F) }
    val parkingAPIClient: PichuParkingAPIClient = remember {
        PichuParkingAPIClient()
    }
    var parkingLotData by remember { mutableStateOf<List<PichuParkingLots>?>(null) }
    var parkingRates by remember { mutableStateOf<List<PichuParkingRates>?>(null) }
    var requirePermissionPrompt by remember { mutableStateOf(true) }
    LaunchedEffect(parkingAPIClient) {
        parkingRates = parkingAPIClient.getParkingRates()
    }
    Box {
        CheckLocationPermissions(fineLocationPermissionState)
        DisplayGoogleMaps(
            cameraPositionState = cameraPositionState,
            enableLocation = fineLocationPermissionState.allPermissionsGranted,
            parkingLotData = parkingLotData,
            parkingRateData = parkingRates,
        )
        FloatingActionButton(
            onClick = {
                if (fineLocationPermissionState.allPermissionsGranted) {
                    scope.launch(Dispatchers.IO) {
                        currentLatLon = fetchCurrentLocation(locationClient)
                    }
                    requirePermissionPrompt = false
                    currentZoom = cameraPositionState.position.zoom
                    cameraPositionState.move(cameraUpdate(currentLatLon, currentZoom))
                } else {
                    Toast.makeText(context, "Location Permissions not enabled", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            modifier = Modifier
                .size(75.dp)
                .padding(16.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
        ) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Current Location")
        }
        FloatingActionButton(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    parkingLotData = parkingAPIClient.getParkingLots()
                }
            },
            modifier = Modifier
                .size(75.dp)
                .padding(start = 16.dp, bottom = 30.dp, end = 16.dp)
                .align(Alignment.BottomStart)
                .clip(
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh, contentDescription = "Refresh Parking Lot Data"
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckLocationPermissions(fineLocationPermissionState: MultiplePermissionsState) {
    var requirePermissionPrompt by remember { mutableStateOf(true) }
    requirePermissionPrompt = !fineLocationPermissionState.allPermissionsGranted
    if (requirePermissionPrompt) {
        LocationPermissionRequest(fineLocationPermissionState) {
            requirePermissionPrompt = false
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionRequest(
    fineLocationPermissionState: MultiplePermissionsState, onDismiss: () -> Unit
) {
    var rationaleState by rememberSaveable {
        mutableStateOf<RationaleState?>(null)
    }
    rationaleState?.run { PermissionAlertDialog(rationaleState = this) }
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
        LaunchedEffect(fineLocationPermissionState) {
            fineLocationPermissionState.launchMultiplePermissionRequest()
        }
    }
    onDismiss()
}

@SuppressLint("MissingPermission")
private suspend fun fetchCurrentLocation(locationClient: FusedLocationProviderClient): LatLng {
    lateinit var currentLatLon: LatLng
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
    return currentLatLon
}

private fun cameraUpdate(latLng: LatLng, zoom: Float): CameraUpdate {
    return CameraUpdateFactory.newCameraPosition(
        CameraPosition.fromLatLngZoom(
            latLng, zoom
        )
    )
}

@Composable
fun DisplayGoogleMaps(
    cameraPositionState: CameraPositionState,
    enableLocation: Boolean = false,
    parkingLotData: List<PichuParkingLots>? = null,
    parkingRateData: List<PichuParkingRates>? = null,
) {
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false, mapToolbarEnabled = false
            )
        )
    }
    val mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = enableLocation)) }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = mapProperties,
    ) {
        val parkingIcon =
            defaultParkingIconFromResource(resourceID = R.drawable.parking_marker, sizeDp = 30.dp)
        parkingLotData?.forEach {
            ParkingMarkerInfoWindow(state = MarkerState(
                position = LatLng(
                    it.latitude, it.longitude
                )
            ),
                title = it.carparkName,
                icon = parkingIcon,
                parkingData = it,
                ratesData = parkingRateData?.let { rates -> getParkingRateForLot(it, rates) })
        }
    }
}

fun getParkingRateForLot(
    parkingLotData: PichuParkingLots, parkingRateData: List<PichuParkingRates>
): List<PichuParkingRates> {
    val carparkID = parkingLotData.carparkID
    return parkingRateData.filter { data -> data.carparkID == carparkID }
}

@Composable
fun ParkingMarkerInfoWindow(
    state: MarkerState,
    title: String? = null,
    icon: BitmapDescriptor? = null,
    parkingData: PichuParkingLots,
    ratesData: List<PichuParkingRates>?,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var displayState by remember(showBottomSheet) { mutableStateOf<BottomSheetDisplayState?>(null) }
    displayState?.run {
        ParkingInfoBottomSheet(
            parkingData = parkingData,
            ratesData = ratesData,
            showBottomSheet = showBottomSheet,
            displayState = this,
        )
    }
    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            displayState = BottomSheetDisplayState { newDisplayState ->
                showBottomSheet = newDisplayState
            }
        }
    }
    MarkerInfoWindow(state = state, title = title, icon = icon, onClick = {
        showBottomSheet = true
        true
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingInfoBottomSheet(
    parkingData: PichuParkingLots,
    ratesData: List<PichuParkingRates>?,
    showBottomSheet: Boolean,
    displayState: BottomSheetDisplayState,
) {
    val sheetState = rememberModalBottomSheetState()
    val sheetColor = MaterialTheme.colorScheme.primaryContainer
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { displayState.onDisplayStateChange(false) },
            sheetState = sheetState,
            containerColor = sheetColor,
            modifier = Modifier.fillMaxSize()
        ) {
            ParkingInfoTabs(parkingData = parkingData, ratesData = ratesData)
        }
    }
}

@Composable
fun ParkingInfoTabs(parkingData: PichuParkingLots, ratesData: List<PichuParkingRates>?) {
    var tabIndex by remember { mutableStateOf(0) }
    val textColor = MaterialTheme.colorScheme.primary
    val tabs = listOf("Details", "Rates")
    Column(modifier = Modifier.padding(10.dp)) {
        ParkingInfoTitle(parkingData = parkingData, modifier = Modifier.padding(bottom = 10.dp))
        TabRow(selectedTabIndex = tabIndex, modifier = Modifier.padding(bottom = 10.dp)) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                )
            }
        }
        when (tabIndex) {
            0 -> ParkingLotDetails(parkingData, textColor)
            1 -> ParkingLotRates(ratesData, textColor)
        }
    }
}

@Composable
fun ParkingInfoTitle(
    parkingData: PichuParkingLots, currentLocation: LatLng = LatLng(0.0, 0.0), modifier: Modifier
) {
    val distance: String = getDistanceStringFromCurrent(parkingData, currentLocation)
    val titleColor = MaterialTheme.colorScheme.primary
    Column(modifier = modifier) {
        Text(
            parkingData.carparkName,
            fontSize = 20.sp,
            textAlign = TextAlign.Left,
            color = titleColor,
            fontWeight = FontWeight.Bold,
        )
        Text("$distance Away", color = titleColor)
        Text(
            "Data Provided By: ${parkingData.agency}",
            color = titleColor,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 10.dp, end = 10.dp)
        )

    }
}

fun getDistanceStringFromCurrent(parkingData: PichuParkingData, currentLocation: LatLng): String {
    val parkingLocation = LatLng(parkingData.latitude, parkingData.longitude)
    val distanceKM = distanceBetweenTwoCoordinates(currentLocation, parkingLocation)
    val roundedDistance: Double = (distanceKM * 100.0).roundToInt() / 100.0
    return formatDistance(roundedDistance)
}

fun formatDistance(distanceKM: Double): String {
    val distanceM = distanceKM / 1000.0
    return if (distanceM < 1) {
        "$distanceM m"
    } else {
        "$distanceKM km"
    }
}

@Composable
fun ParkingLotDetails(parkingData: PichuParkingLots, textColor: Color) {
    Column {
        Text("Carpark ID: ${parkingData.carparkID}", color = textColor)
        Text("Vehicle Type: ${parkingData.translatedVehicleCategory}", color = textColor)
        Text("Available Lots: ${parkingData.availableLots}", color = textColor)
    }
}

@Composable
fun ParkingLotRates(parkingRates: List<PichuParkingRates>? = null, textColor: Color) {
    Column {
        parkingRates?.forEach {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Column(modifier = Modifier.padding(5.dp)) {
                    Text("Time Period: ${it.timeRange}", color = textColor)
                    Text("Weekday: ${it.weekdayRate} per ${it.weekdayMin}", color = textColor)
                    Text("Saturday: ${it.saturdayRate} per ${it.saturdayMin}", color = textColor)
                    Text(
                        "Sundays & PH: ${it.sundayPHRate} per ${it.sundayPHMin}", color = textColor
                    )
                }
            }
        }
    }
}

data class BottomSheetDisplayState(
    val onDisplayStateChange: (showBottomSheet: Boolean) -> Unit
)


@Composable
private fun defaultParkingIconFromResource(resourceID: Int, sizeDp: Dp): BitmapDescriptor? {
    val context = LocalContext.current
    val parkingIconDrawable = ContextCompat.getDrawable(context, resourceID)
    val sizePx = with(LocalDensity.current) { sizeDp.toPx().toInt() }

    parkingIconDrawable?.setBounds(0, 0, sizePx, sizePx)
    val parkingIconBitmap =
        parkingIconDrawable?.let { Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888) }
    val canvas: Canvas? = parkingIconBitmap?.let { Canvas(it) }
    if (canvas != null) {
        parkingIconDrawable.draw(canvas)
    }
    return parkingIconBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }
}

@Composable
fun MapOptionsButton() {
    var showBottomSheet by remember { mutableStateOf(false) }
    var displayState by remember(showBottomSheet) { mutableStateOf<BottomSheetDisplayState?>(null) }
    displayState?.run {
        MapOptionsScreen(
            showBottomSheet = showBottomSheet,
            displayState = this,
        )
    }
    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            displayState = BottomSheetDisplayState { newDisplayState ->
                showBottomSheet = newDisplayState
            }
        }
    }
    Column {
        Button(
            onClick = { showBottomSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Map Options")
        }
    }
}

@Composable
fun MapOptionItem(option: MapOption, onOptionSelected: (MapOption) -> Unit) {
    Box(modifier = Modifier
        .padding(8.dp)
        .clickable { onOptionSelected(option) }) {
        Image(
            painter = painterResource(id = option.iconResId),
            contentDescription = option.title,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        )
    }
}

data class MapOption(val title: String, val iconResId: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapOptionsScreen(
    showBottomSheet: Boolean,
    displayState: BottomSheetDisplayState
) {
    val sheetState = rememberModalBottomSheetState()
    val sheetColor = MaterialTheme.colorScheme.primaryContainer
    val mapOptions = listOf(
        MapOption("Display Markers", R.drawable.test),
//        MapOption("Function 1", R.drawable.ic_function1),
//        MapOption("Function 2", R.drawable.ic_function2),
        // Add more options as needed
    )
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { displayState.onDisplayStateChange(false) },
            sheetState = sheetState,
            containerColor = sheetColor,
            modifier = Modifier.fillMaxSize()
        ) {
            FilterOptions(options = mapOptions, onOptionSelected = { selectedOption ->
                // Handle the selected option here

            })
        }
    }


}


@Composable
fun FilterOptions(options: List<MapOption>, onOptionSelected: (MapOption) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Select an option",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp)
        ) {
            items(options) { option ->
                MapOptionItem(option, onOptionSelected)
            }
        }
    }
}