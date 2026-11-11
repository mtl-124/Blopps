package com.lajara.blopps.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.PublicacionPopular
import com.lajara.blopps.ui.theme.BloppsDorado
import com.lajara.blopps.ui.theme.BloppsError
import com.lajara.blopps.ui.theme.BloppsMorado
import com.lajara.blopps.ui.theme.BloppsMoradoClaro

@Composable
fun AdminScreen(viewModel: AdminViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmarLimpieza by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.actualizarMetricas() }
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { texto ->
            snackbar.showSnackbar(texto)
            viewModel.mensajeMostrado()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Encabezado()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Metrica(
                    modifier = Modifier.weight(1f),
                    icono = Icons.Filled.Movie,
                    valor = estado.totalGuardadas.toString(),
                    etiqueta = "Reseñas guardadas"
                )
                Metrica(
                    modifier = Modifier.weight(1f),
                    icono = Icons.Filled.Favorite,
                    valor = estado.totalFavoritas.toString(),
                    etiqueta = "Favoritos de todas las cuentas"
                )
            }
            TarjetaComunidades(
                estado = estado,
                onSincronizar = viewModel::sincronizarComunidad
            )
            TarjetaMasFavoritas(populares = estado.masFavoritas)
            TarjetaHerramientas(
                estado = estado,
                onSincronizarTodo = viewModel::sincronizar,
                onLimpiar = { confirmarLimpieza = true }
            )
            Spacer(Modifier.height(8.dp))
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    if (confirmarLimpieza) {
        AlertDialog(
            onDismissRequest = { confirmarLimpieza = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("¿Limpiar caché?") },
            text = {
                Text(
                    "Se borrarán las reseñas guardadas en este teléfono. " +
                            "Los favoritos de cada cuenta se conservan y vuelven al sincronizar."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarLimpieza = false
                        viewModel.limpiarCache()
                    }
                ) {
                    Text("Limpiar", color = BloppsError)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpieza = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun Encabezado() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF5B21B6), BloppsMorado))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AdminPanelSettings,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Column {
            Text(
                text = buildAnnotatedString {
                    append("Panel de ")
                    withStyle(SpanStyle(color = BloppsDorado)) { append("administración") }
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Métricas y herramientas de datos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Metrica(
    modifier: Modifier,
    icono: ImageVector,
    valor: String,
    etiqueta: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = BloppsMoradoClaro,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TarjetaPanel(
    titulo: String,
    icono: ImageVector,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = BloppsMoradoClaro,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            contenido()
        }
    }
}

@Composable
private fun TarjetaComunidades(
    estado: AdminUiState,
    onSincronizar: (Comunidad) -> Unit
) {
    TarjetaPanel(titulo = "Reseñas por comunidad", icono = Icons.Filled.BarChart) {
        Comunidad.entries.forEach { comunidad ->
            val cantidad = estado.porComunidad[comunidad] ?: 0
            val fraccion = if (estado.totalGuardadas == 0) {
                0f
            } else {
                cantidad.toFloat() / estado.totalGuardadas
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = comunidad.icono(),
                        contentDescription = null,
                        tint = BloppsMoradoClaro,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = comunidad.titulo,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = cantidad.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = BloppsDorado
                    )
                    IconButton(
                        onClick = { onSincronizar(comunidad) },
                        enabled = !estado.sincronizando,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "Sincronizar ${comunidad.titulo}",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { fraccion },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TarjetaMasFavoritas(populares: List<PublicacionPopular>) {
    TarjetaPanel(titulo = "Más favoritas", icono = Icons.Filled.Favorite) {
        if (populares.isEmpty()) {
            Text(
                text = "Aún nadie marcó favoritos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            populares.forEachIndexed { indice, popular ->
                val publicacion = popular.publicacion
                val titulo = (publicacion.pelicula?.titulo ?: publicacion.titulo)
                    .replaceFirstChar { it.uppercase() }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(BloppsMorado.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (indice + 1).toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = BloppsMoradoClaro
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = publicacion.comunidad.titulo,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = popular.totalFavoritos.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaHerramientas(
    estado: AdminUiState,
    onSincronizarTodo: () -> Unit,
    onLimpiar: () -> Unit
) {
    TarjetaPanel(titulo = "Herramientas de datos", icono = Icons.Filled.Sync) {
        Text(
            text = textoUltimaSincronizacion(estado.ultimaSincronizacion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onSincronizarTodo,
            enabled = !estado.sincronizando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            if (estado.sincronizando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
                Text("Sincronizando…", fontWeight = FontWeight.SemiBold)
            } else {
                Icon(Icons.Filled.Sync, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("Sincronizar todo", fontWeight = FontWeight.SemiBold)
            }
        }
        OutlinedButton(
            onClick = onLimpiar,
            enabled = !estado.sincronizando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, BloppsError.copy(alpha = 0.6f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BloppsError)
        ) {
            Icon(Icons.Filled.DeleteSweep, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("Limpiar caché", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun textoUltimaSincronizacion(momento: Long?): String {
    if (momento == null) return "Aún no sincronizas en esta sesión"
    val minutos = (System.currentTimeMillis() - momento) / 60_000
    return when {
        minutos < 1 -> "Última sincronización: hace un momento"
        minutos < 60 -> "Última sincronización: hace $minutos min"
        else -> "Última sincronización: hace ${minutos / 60} h"
    }
}

private fun Comunidad.icono(): ImageVector = when (this) {
    Comunidad.ACCION -> Icons.Filled.LocalFireDepartment
    Comunidad.COMEDIA -> Icons.Filled.Mood
    Comunidad.CIENCIA_FICCION -> Icons.Filled.RocketLaunch
}