package pt.ua.icm.refinder.ui.screens.report

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ua.icm.refinder.data.model.itemCategories
import pt.ua.icm.refinder.ui.theme.ReFinderTheme
import pt.ua.icm.refinder.ui.theme.RefinderAccent
import pt.ua.icm.refinder.ui.theme.RefinderBackground
import pt.ua.icm.refinder.ui.theme.RefinderSurface
import pt.ua.icm.refinder.ui.theme.RefinderSurfaceLight
import pt.ua.icm.refinder.ui.theme.RefinderTextMuted
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import android.location.Geocoder
import androidx.compose.material.icons.filled.Search
import coil.compose.rememberAsyncImagePainter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: ReportViewModel = viewModel()) {



    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedTypeIndex by remember { mutableIntStateOf(0) }
    val types = listOf("Perdido", "Achado")
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    var selectedCategory by remember { mutableStateOf("Outro") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val isLoading = viewModel.isLoading
    val successMessage = viewModel.successMessage
    val errorMessage = viewModel.errorMessage

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempUri
        }
    }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = getTempUri(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permissão da câmara negada", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            getCurrentLocation(
                context = context,
                fusedLocationClient = fusedLocationClient,
                onLocationReceived = { location ->
                    latitude = location?.latitude
                    longitude = location?.longitude
                },
                onError = {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val config = org.osmdroid.config.Configuration.getInstance()
        config.userAgentValue = context.packageName
        config.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            getCurrentLocation(
                context = context,
                fusedLocationClient = fusedLocationClient,
                onLocationReceived = { location ->
                    latitude = location?.latitude
                    longitude = location?.longitude
                },
                onError = {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun launchCamera() {
        val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            val uri = getTempUri(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Date picker state
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = datePickerState.selectedDateMillis?.let {
        dateFormatter.format(Date(it))
    } ?: ""
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()

            title = ""
            description = ""
            location = ""
            imageUri = null
            selectedTypeIndex = 0

            viewModel.clearMessages()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Escolher fonte da imagem") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Câmara") },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showImageSourceDialog = false
                            launchCamera()
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Galeria") },
                        leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RefinderBackground)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 96.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Registar item",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Adiciona uma foto, localização e detalhes para ajudar a comunidade.",
                style = MaterialTheme.typography.bodyLarge,
                color = RefinderTextMuted
            )
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            types.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                    onClick = { selectedTypeIndex = index },
                    selected = index == selectedTypeIndex
                ) {
                    Text(label)
                }
            }
        }

        // TITLE
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nome do item") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = refinderTextFieldColors()
        )

        // CATEGORY
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoria") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                shape = RoundedCornerShape(18.dp),
                colors = refinderTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                itemCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // IMAGE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clickable { showImageSourceDialog = true },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = RefinderSurface
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = RefinderAccent
                        )

                        Text(
                            text = "Adicionar foto do item",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Câmara ou galeria",
                            color = RefinderTextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // GENERATE DESCRIPTION BUTTON
        if (imageUri != null) {
            OutlinedButton(
                onClick = {
                    val bitmap = uriToBitmap(context, imageUri!!)

                    viewModel.generateAiDescription(bitmap) { generatedText ->
                        description = generatedText
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !viewModel.isAiLoading,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RefinderAccent
                )
            ) {
                if (viewModel.isAiLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = RefinderAccent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Gerar descrição com IA",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // DESCRIPTION
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Adicione uma descrição") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(18.dp),
            colors = refinderTextFieldColors()
        )

        // LOCAL SEARCH
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Local") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    if (location.isNotBlank()) {
                        searchLocation(context, location) { lat, lon ->
                            latitude = lat
                            longitude = lon
                        }
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search Location")
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = refinderTextFieldColors()
        )

        // MAP
        Text(
            text = "Localização GPS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .pointerInput(Unit) {
                    detectDragGestures { _, _ -> }
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = RefinderSurface
            )
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        setBuiltInZoomControls(false)
                        controller.setZoom(15.0)

                        val startPoint = if (latitude != null && longitude != null) {
                            GeoPoint(latitude!!, longitude!!)
                        } else {
                            GeoPoint(40.6331, -8.6596)
                        }

                        controller.setCenter(startPoint)

                        val receiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                latitude = p.latitude
                                longitude = p.longitude

                                val geocoder = Geocoder(context, Locale.getDefault())

                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        geocoder.getFromLocation(p.latitude, p.longitude, 1) { addresses ->
                                            addresses.firstOrNull()?.let { addr ->
                                                location = addr.getAddressLine(0) ?: ""
                                            }
                                        }
                                    } else {
                                        val addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1)
                                        addresses?.firstOrNull()?.let { addr ->
                                            location = addr.getAddressLine(0) ?: ""
                                        }
                                    }
                                } catch (e: Exception) {
                                    // ignore geocoding errors
                                }

                                return true
                            }

                            override fun longPressHelper(p: GeoPoint): Boolean = false
                        }

                        overlays.add(MapEventsOverlay(receiver))
                    }
                },
                update = { mapView ->
                    if (latitude != null && longitude != null) {
                        val point = GeoPoint(latitude!!, longitude!!)

                        mapView.controller.animateTo(point)

                        val existingMarkers = mapView.overlays.filterIsInstance<Marker>()
                        mapView.overlays.removeAll(existingMarkers)

                        val marker = Marker(mapView).apply {
                            position = point
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setTitle("Local selecionado")
                        }

                        mapView.overlays.add(marker)
                        marker.showInfoWindow()
                        mapView.invalidate()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // GPS
        Text(
            text = if (latitude != null && longitude != null) {
                "GPS: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}"
            } else {
                "GPS não disponível"
            },
            style = MaterialTheme.typography.bodySmall,
            color = RefinderTextMuted,
            modifier = Modifier.align(Alignment.Start)
        )

        // DATE
        OutlinedTextField(
            value = dateString,
            onValueChange = { },
            label = { Text("Data") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            readOnly = true,
            enabled = false,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = refinderTextFieldColors()
        )


        Button(
            onClick = {
                val selectedType = if (selectedTypeIndex == 0) "lost" else "found"

                when {
                    title.isBlank() -> {
                        Toast.makeText(context, "Introduza o nome do item", Toast.LENGTH_SHORT).show()
                    }
                    location.isBlank() -> {
                        Toast.makeText(context, "Introduza o local", Toast.LENGTH_SHORT).show()
                    }
                    dateString.isBlank() -> {
                        Toast.makeText(context, "Selecione uma data", Toast.LENGTH_SHORT).show()
                    }
                    description.isBlank() -> {
                        Toast.makeText(context, "Introduza uma descrição", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        viewModel.submitItem(
                            title = title,
                            description = description,
                            type = selectedType,
                            category = selectedCategory,
                            locationName = location,
                            date = dateString,
                            latitude = latitude,
                            longitude = longitude,
                            imageUri = imageUri
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 8.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RefinderAccent,
                contentColor = RefinderBackground
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = RefinderBackground
                )
            } else {
                Text(
                    text = "Registar Objeto ${types[selectedTypeIndex]}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getTempUri(context: Context): Uri {
    val tempFile = File.createTempFile("refinder_image_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        tempFile
    )
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}
@Composable
private fun refinderTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = RefinderAccent,
    unfocusedBorderColor = Color(0xFF2A3145),
    focusedLabelColor = RefinderAccent,
    unfocusedLabelColor = RefinderTextMuted,
    cursorColor = RefinderAccent,
    focusedContainerColor = RefinderSurface,
    unfocusedContainerColor = RefinderSurface,
    disabledTextColor = Color.White,
    disabledBorderColor = Color(0xFF2A3145),
    disabledLabelColor = RefinderTextMuted,
    disabledTrailingIconColor = RefinderTextMuted
)

@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit,
    onError: (String) -> Unit
) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            onLocationReceived(location)
        }
        .addOnFailureListener { e ->
            onError(e.message ?: "Erro ao obter localização")
        }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ReFinderTheme {
        Surface {
            ReportScreen()
        }
    }
}

fun searchLocation(context: Context, query: String, onResult: (Double, Double) -> Unit) {
    val geocoder = Geocoder(context)
    try {
        // Modern Android (SDK 33+) uses a listener, older uses a blocking call
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(query, 1) { addresses ->
                val address = addresses.firstOrNull()
                if (address != null) {
                    onResult(address.latitude, address.longitude)
                }
            }
        } else {
            // Fallback for older versions
            val addresses = geocoder.getFromLocationName(query, 1)
            val address = addresses?.firstOrNull()
            if (address != null) {
                onResult(address.latitude, address.longitude)
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Local não encontrado", Toast.LENGTH_SHORT).show()
    }
}