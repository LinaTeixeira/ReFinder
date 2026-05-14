package pt.ua.icm.refinder.ui.screens.map

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.ui.screens.search.SearchViewModel

@Composable
fun MapScreen(
    onItemClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val items = viewModel.allItems

    // Configuração inicial do OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    // Centrar na UA por defeito
                    controller.setCenter(GeoPoint(40.6332, -8.6594))
                }
            },
            update = { mapView ->
                mapView.overlays.clear()
                
                items.forEach { item ->
                    if (item.latitude != null && item.longitude != null) {
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(item.latitude, item.longitude)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = item.title
                        marker.subDescription = "${if (item.type == "lost") "Perdido" else "Achado"} - ${item.category}"
                        
                        marker.setOnMarkerClickListener { m, _ ->
                            onItemClick(item.id)
                            true
                        }
                        
                        mapView.overlays.add(marker)
                    }
                }
                mapView.invalidate()
            }
        )
    }
}
