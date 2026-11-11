package com.lajara.blopps.ui.crear

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.LimitesPublicacion
import com.lajara.blopps.domain.model.Sesion
import com.lajara.blopps.ui.components.CalificacionEstrellas
import com.lajara.blopps.ui.theme.BloppsDorado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPublicacionScreen(
    sesion: Sesion,
    viewModel: CrearPublicacionViewModel,
    comunidadInicial: Comunidad?,
    onAtras: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        if (comunidadInicial != null && estado.comunidad == null) {
            viewModel.elegirComunidad(comunidadInicial)
        }
    }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limpiarMensaje()
        }
    }

    LaunchedEffect(estado.publicada) {
        if (estado.publicada) {
            viewModel.consumirPublicada()
            onAtras()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nueva publicación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Comunidad
            Text("Comunidad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Comunidad.entries.forEach { comunidad ->
                    FilterChip(
                        selected = estado.comunidad == comunidad,
                        onClick = { viewModel.elegirComunidad(comunidad) },
                        label = { Text(comunidad.titulo) }
                    )
                }
            }

            // Película
            Text("Película", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (!estado.busquedaDisponible) {
                OutlinedCard(
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = "La búsqueda de películas no está disponible",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else if (estado.peliculaElegida == null) {
                OutlinedTextField(
                    value = estado.consulta,
                    onValueChange = viewModel::cambiarConsulta,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar película") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (estado.consulta.isNotBlank()) {
                            IconButton(onClick = { viewModel.cambiarConsulta("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )

                if (estado.buscando) {
                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }

                estado.errorBusqueda?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                if (estado.consulta.trim().length >= 2 && !estado.buscando && estado.errorBusqueda == null && estado.resultados.isEmpty()) {
                    Text(
                        text = "Sin resultados para «${estado.consulta}»",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (estado.resultados.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        estado.resultados.take(10).forEach { pelicula ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        keyboardController?.hide()
                                        viewModel.elegirPelicula(pelicula)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(56.dp)
                                            .height(84.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Movie, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        pelicula.posterUrl?.let { url ->
                                            AsyncImage(
                                                model = url,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pelicula.titulo,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            pelicula.anio?.let { anio ->
                                                Text(text = anio.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = BloppsDorado, modifier = Modifier.size(14.dp))
                                            Text(text = "%.1f".format(pelicula.calificacion), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = pelicula.sinopsis.ifBlank { "Sinopsis no disponible" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(84.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            estado.peliculaElegida?.posterUrl?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = estado.peliculaElegida?.titulo.orEmpty(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            estado.peliculaElegida?.anio?.let { anio ->
                                Text(
                                    text = anio.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(onClick = viewModel::quitarPelicula) {
                            Text("Cambiar")
                        }
                    }
                }
            }

            // Título
            OutlinedTextField(
                value = estado.titulo,
                onValueChange = viewModel::cambiarTitulo,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título de tu publicación") },
                supportingText = {
                    Text("${estado.titulo.length}/${LimitesPublicacion.TITULO_MAX}")
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Reseña
            OutlinedTextField(
                value = estado.resena,
                onValueChange = viewModel::cambiarResena,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tu reseña") },
                supportingText = {
                    Text("${estado.resena.length}/${LimitesPublicacion.RESENA_MAX}")
                },
                minLines = 5,
                maxLines = 10,
                shape = RoundedCornerShape(16.dp)
            )

            // Calificación
            Text("Calificación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            CalificacionEstrellas(
                valor = estado.calificacion,
                onCambio = viewModel::elegirCalificacion,
                tamano = 36.dp
            )

            Spacer(Modifier.height(8.dp))

            // Botón Publicar
            Button(
                onClick = { viewModel.publicar(sesion) },
                enabled = !estado.publicando,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                if (estado.publicando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Publicar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
