package pt.ua.icm.refinder.ui.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pt.ua.icm.refinder.ui.screens.search.SearchViewModel
import pt.ua.icm.refinder.ui.theme.FoundColor
import pt.ua.icm.refinder.ui.theme.LostColor
import pt.ua.icm.refinder.ui.theme.RefinderAccent
import pt.ua.icm.refinder.ui.theme.RefinderBackground
import pt.ua.icm.refinder.ui.theme.RefinderSurface
import pt.ua.icm.refinder.ui.theme.RefinderTextMuted

@Composable
fun MapScreen(
    onItemClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val items = viewModel.allItems
    val itemsWithLocation = items.filter {
        it.latitude != null &&
                it.longitude != null &&
                it.status != "claimed"
    }
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RefinderBackground)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(40.6332, -8.6594))
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                itemsWithLocation.forEach { item ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(item.latitude!!, item.longitude!!)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        icon = createCustomMarkerIcon(
                            context = context,
                            color = when {
                                item.status == "deposited" -> RefinderAccent
                                item.type == "lost" -> LostColor
                                else -> FoundColor
                            }
                        )

                        title = item.title
                        subDescription =
                            "${if (item.type == "lost") "Perdido" else "Achado"} • ${item.category}"

                        setOnMarkerClickListener { _, _ ->
                            onItemClick(item.id)
                            true
                        }
                    }

                    mapView.overlays.add(marker)
                }

                mapView.invalidate()
            }
        )

        MapTopPanel(
            total = itemsWithLocation.size,
            lost = itemsWithLocation.count { it.type == "lost" },
            found = itemsWithLocation.count { it.type == "found" },
            deposited = itemsWithLocation.count { it.status == "deposited" },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )

        MapLegend(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 22.dp)
        )
    }
}

@Composable
private fun MapTopPanel(
    total: Int,
    lost: Int,
    found: Int,
    deposited: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = RefinderSurface.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = RefinderAccent.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = RefinderAccent,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Column {
                    Text(
                        text = "Mapa de itens",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "Explora objetos registados perto da UA.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RefinderTextMuted
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStat("Total", total.toString(), Modifier.weight(1f))
                MiniStat("Perdidos", lost.toString(), Modifier.weight(1f))
                MiniStat("Achados", found.toString(), Modifier.weight(1f))
                MiniStat("Cacifo", deposited.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF202638)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RefinderAccent
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = RefinderTextMuted
            )
        }
    }
}

@Composable
private fun MapLegend(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = RefinderSurface.copy(alpha = 0.94f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(
                color = LostColor,
                label = "Perdido"
            )

            LegendItem(
                color = FoundColor,
                label = "Achado"
            )

            LegendItem(
                color = RefinderAccent,
                label = "No cacifo"
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = RoundedCornerShape(999.dp))
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = RefinderTextMuted
        )
    }
}

private fun createCustomMarkerIcon(
    context: Context,
    color: Color
): BitmapDrawable {
    val width = 90
    val height = 110
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.rgb(
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        style = Paint.Style.FILL
    }

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    val centerX = width / 2f
    val circleRadius = 34f
    val circleY = 38f

    canvas.drawCircle(centerX, circleY, circleRadius, paint)
    canvas.drawCircle(centerX, circleY, circleRadius, borderPaint)

    val path = Path().apply {
        moveTo(centerX - 18f, circleY + 22f)
        lineTo(centerX + 18f, circleY + 22f)
        lineTo(centerX, height - 8f)
        close()
    }

    canvas.drawPath(path, paint)

    val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    canvas.drawCircle(centerX, circleY, 13f, whitePaint)

    return BitmapDrawable(context.resources, bitmap)
}
