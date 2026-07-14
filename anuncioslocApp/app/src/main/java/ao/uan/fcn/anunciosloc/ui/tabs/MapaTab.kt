package ao.uan.fcn.anunciosloc.ui.tabs

import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ao.uan.fcn.anunciosloc.core.utils.Constants
import ao.uan.fcn.anunciosloc.data.model.Infraestrutura
import ao.uan.fcn.anunciosloc.data.repository.LocalRepository
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

fun GeoPoint.circlePoints(radiusMeters: Double, numPoints: Int = 48): List<GeoPoint> {
    val points = mutableListOf<GeoPoint>()
    val earthRadius = 6371000.0
    for (i in 0 until numPoints) {
        val angle = 2 * Math.PI * i / numPoints
        val dx = radiusMeters * Math.cos(angle)
        val dy = radiusMeters * Math.sin(angle)
        val latOffset = (dy / earthRadius) * (180 / Math.PI)
        val lonOffset = (dx / (earthRadius * Math.cos(Math.toRadians(this.latitude)))) * (180 / Math.PI)
        points.add(GeoPoint(this.latitude + latOffset, this.longitude + lonOffset))
    }
    return points
}

@Composable
fun MapaTab(
    token: String,
    localRepository: LocalRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var infraestruturas by remember { mutableStateOf<List<Infraestrutura>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    val centro = GeoPoint(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE)

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        carregarInfraestruturas()
    }

    fun carregarInfraestruturas() {
        scope.launch {
            isLoading = true
            localRepository.listarInfraestruturas(
                Constants.DEFAULT_LATITUDE,
                Constants.DEFAULT_LONGITUDE,
                20
            ).onSuccess {
                infraestruturas = it
                Log.d("MapaTab", "Recebidas ${it.size}")
            }.onFailure {
                Toast.makeText(context, "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
            }
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            infraestruturas.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma infraestrutura encontrada.")
            }
            else -> {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setBuiltInZoomControls(true)
                            setMultiTouchControls(true)
                            controller.setZoom(13.0)
                            controller.setCenter(centro)
                            atualizarOverlays(this, infraestruturas)
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.clear()
                        atualizarOverlays(mapView, infraestruturas)
                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = Color(0xFF2196F3)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar local")
                }
            }
        }
    }

    if (showAddDialog) {
        AddLocalDialog(
            infraestruturas = infraestruturas.map { it.nome },
            onDismiss = { showAddDialog = false },
            onSave = { infra, nome, lat, lon, raio ->
                scope.launch {
                    val result = localRepository.criarLocal(token, infra, nome, lat, lon, raio)
                    result.onSuccess {
                        Toast.makeText(context, "Local criado!", Toast.LENGTH_SHORT).show()
                        showAddDialog = false
                        carregarInfraestruturas()
                    }.onFailure {
                        Toast.makeText(context, "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

private fun atualizarOverlays(mapView: MapView, infraestruturas: List<Infraestrutura>) {
    infraestruturas.forEach { infra ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(infra.latitude, infra.longitude)
            title = infra.nome
            setSubDescription("Cap: ${infra.capacidade} | Livres: ${infra.conexoesDisponiveis}")
        }
        mapView.overlays.add(marker)

        val circle = Polygon(mapView).apply {
            val points = GeoPoint(infra.latitude, infra.longitude)
                .circlePoints(infra.raio.toDouble(), 48)
            setPoints(points)
            outlinePaint.color = Color.argb(80, 0, 150, 255)
            outlinePaint.strokeWidth = 2f
            fillPaint.color = Color.argb(30, 0, 150, 255)
            fillPaint.style = android.graphics.Paint.Style.FILL_AND_STROKE
        }
        mapView.overlays.add(circle)
    }
}