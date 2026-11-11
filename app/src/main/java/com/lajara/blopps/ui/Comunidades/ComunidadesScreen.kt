package com.lajara.blopps.ui.comunidades

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.Publicacion
import com.lajara.blopps.ui.UiState
import com.lajara.blopps.ui.components.BloppsIcono
import com.lajara.blopps.ui.components.BloppsWordmark
import com.lajara.blopps.ui.components.CargandoView
import com.lajara.blopps.ui.components.ErrorView
import com.lajara.blopps.ui.components.PublicacionCard
import com.lajara.blopps.ui.components.VacioView
import com.lajara.blopps.ui.theme.BloppsDorado
import com.lajara.blopps.ui.theme.BloppsVerde

@Composable
fun ComunidadesScreen(
    viewModel: PublicacionesViewModel,
    modifier: Modifier = Modifier,
    onAbrirDetalle: (Int) -> Unit = {}
) {
    val comunidad by viewModel.comunidad.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    var buscando by rememberSaveable { mutableStateOf(false) }
    var consulta by rememberSaveable { mutableStateOf("") }

    val visibles = remember(estado, consulta) {
        val lista = (estado as? UiState.Exito)?.data.orEmpty()
        if (consulta.isBlank()) lista else lista.filter { p ->
            p.titulo.contains(consulta, ignoreCase = true) ||
                    p.cuerpo.contains(consulta, ignoreCase = true) ||
                    p.pelicula?.titulo?.contains(consulta, ignoreCase = true) == true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Encabezado(
                    buscando = buscando,
                    onBuscar = {
                        buscando = !buscando
                        if (!buscando) consulta = ""
                    }
                )
            }
            item {
                if (buscando) CampoBusqueda(valor = consulta, onCambio = { consulta = it })
                else Titulos()
            }
            item {
                SelectorComunidades(
                    seleccionada = comunidad,
                    onSeleccionar = viewModel::seleccionarComunidad
                )
            }
            when (val actual = estado) {
                is UiState.Cargando -> item {
                    Box(Modifier.fillParentMaxHeight(0.7f)) { CargandoView() }
                }
                is UiState.Error -> item {
                    Box(Modifier.fillParentMaxHeight(0.7f)) {
                        ErrorView(mensaje = actual.mensaje, onReintentar = viewModel::refrescar)
                    }
                }
                is UiState.Vacio -> item {
                    Box(Modifier.fillParentMaxHeight(0.7f)) {
                        VacioView(
                            titulo = "Aún no hay reseñas",
                            descripcion = "Toca actualizar para traer las de ${comunidad.titulo}",
                            icono = Icons.Filled.Inbox
                        )
                    }
                }
                is UiState.Exito -> {
                    if (visibles.isEmpty()) {
                        item {
                            Box(Modifier.fillParentMaxHeight(0.7f)) {
                                VacioView(
                                    titulo = "Sin resultados",
                                    descripcion = "Prueba con otra palabra",
                                    icono = Icons.Filled.SearchOff
                                )
                            }
                        }
                    } else {
                        items(visibles, key = { it.id }) { publicacion ->
                            PublicacionCard(
                                publicacion = publicacion,
                                onClick = { onAbrirDetalle(publicacion.id) },
                                onFavorita = { viewModel.alternarFavorita(publicacion.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                            )
                        }
                        item { LineaEstado(aviso = aviso) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado(buscando: Boolean, onBuscar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BloppsIcono(tamano = 48.dp)
        Spacer(Modifier.width(10.dp))
        BloppsWordmark(tamano = 32.sp)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onBuscar) {
            Icon(
                imageVector = if (buscando) Icons.Filled.Close else Icons.Filled.Search,
                contentDescription = if (buscando) "Cerrar búsqueda" else "Buscar"
            )
        }
        IconButton(onClick = {}) {
            BadgedBox(badge = { Badge(containerColor = BloppsDorado) }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
            }
        }
    }
}

@Composable
private fun Titulos() {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = buildAnnotatedString {
                append("Explora ")
                withStyle(SpanStyle(color = BloppsDorado)) { append("comunidades") }
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Descubre reseñas de tus películas favoritas",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CampoBusqueda(valor: String, onCambio: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        placeholder = { Text("Buscar reseñas") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun SelectorComunidades(
    seleccionada: Comunidad,
    onSeleccionar: (Comunidad) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Comunidad.entries.forEach { comunidad ->
            val activa = comunidad == seleccionada
            Surface(
                onClick = { onSeleccionar(comunidad) },
                shape = RoundedCornerShape(22.dp),
                color = if (activa) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (activa) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                border = if (activa) {
                    null
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = comunidad.icono(),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = comunidad.titulo, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ListaPublicaciones(
    publicaciones: List<Publicacion>,
    consulta: String,
    onAbrir: (Int) -> Unit,
    onFavorita: (Int) -> Unit
) {
    val visibles = remember(publicaciones, consulta) {
        if (consulta.isBlank()) {
            publicaciones
        } else {
            publicaciones.filter { p ->
                p.titulo.contains(consulta, ignoreCase = true) ||
                        p.cuerpo.contains(consulta, ignoreCase = true) ||
                        p.pelicula?.titulo?.contains(consulta, ignoreCase = true) == true
            }
        }
    }

    if (visibles.isEmpty()) {
        VacioView(
            titulo = "Sin resultados",
            descripcion = "Prueba con otra palabra",
            icono = Icons.Filled.SearchOff
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(visibles, key = { it.id }) { publicacion ->
                PublicacionCard(
                    publicacion = publicacion,
                    onClick = { onAbrir(publicacion.id) },
                    onFavorita = { onFavorita(publicacion.id) }
                )
            }
        }
    }
}

@Composable
private fun LineaEstado(aviso: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (aviso == null) BloppsVerde else BloppsDorado, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = aviso ?: "Datos actualizados",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Comunidad.icono(): ImageVector = when (this) {
    Comunidad.ACCION -> Icons.Filled.LocalFireDepartment
    Comunidad.COMEDIA -> Icons.Filled.Mood
    Comunidad.CIENCIA_FICCION -> Icons.Filled.RocketLaunch
}