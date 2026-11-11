package com.lajara.blopps.ui.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.lajara.blopps.domain.model.Rol
import com.lajara.blopps.domain.model.Sesion
import com.lajara.blopps.ui.UiState
import com.lajara.blopps.ui.components.CalificacionEstrellas
import com.lajara.blopps.ui.components.CargandoView
import com.lajara.blopps.ui.components.ErrorView
import com.lajara.blopps.ui.theme.BloppsMoradoClaro
import com.lajara.blopps.ui.util.fechaRelativa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePublicacionUsuarioScreen(
    sesion: Sesion,
    id: String,
    viewModel: DetallePublicacionUsuarioViewModel,
    socialViewModel: SocialViewModel,
    onAtras: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val eliminando by viewModel.eliminando.collectAsStateWithLifecycle()
    val eliminada by viewModel.eliminada.collectAsStateWithLifecycle()
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        viewModel.cargar(id)
    }

    LaunchedEffect(eliminada) {
        if (eliminada) {
            onAtras()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de publicación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (estado is UiState.Exito) {
                        val pub = (estado as UiState.Exito).data
                        if (sesion.uid == pub.autorUid || sesion.rol == Rol.ADMIN) {
                            IconButton(onClick = { mostrarDialogoEliminar = true }, enabled = !eliminando) {
                                if (eliminando) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar publicación")
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val actual = estado) {
            is UiState.Cargando -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CargandoView()
                }
            }
            is UiState.Vacio -> {}
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    ErrorView(mensaje = actual.mensaje, onReintentar = { viewModel.cargar(id) })
                }
            }
            is UiState.Exito -> {
                val pub = actual.data
                val autorNombre = pub.autorCorreo.substringBefore("@")
                val tiempoRelativo = fechaRelativa(System.currentTimeMillis(), pub.fecha)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Póster grande (2:3 aspect ratio, max width 220.dp)
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF3B2A7A), Color(0xFF14102B)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Movie,
                            contentDescription = null,
                            tint = BloppsMoradoClaro.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        pub.posterUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = pub.titulo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = buildString {
                                append("Reseña de ")
                                append(pub.peliculaTitulo)
                                pub.anio?.let { append(" ($it)") }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CalificacionEstrellas(valor = pub.calificacion, onCambio = null, tamano = 20.dp)
                            Text(
                                text = "Calificación del autor",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Por $autorNombre · $tiempoRelativo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = pub.resena,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    SocialSeccion(
                        publicacionId = pub.id,
                        sesion = sesion,
                        viewModel = socialViewModel
                    )
                }
            }
        }

        if (mostrarDialogoEliminar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoEliminar = false },
                title = { Text("¿Eliminar publicación?") },
                text = { Text("Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            mostrarDialogoEliminar = false
                            viewModel.eliminar(id)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoEliminar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
