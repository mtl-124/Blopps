package com.lajara.blopps.ui.detalle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.PeliculaInfo
import com.lajara.blopps.domain.model.Publicacion
import com.lajara.blopps.domain.model.Sesion
import com.lajara.blopps.ui.UiState
import com.lajara.blopps.ui.components.CargandoView
import com.lajara.blopps.ui.components.ErrorView
import com.lajara.blopps.ui.theme.BloppsDorado
import com.lajara.blopps.ui.theme.BloppsMorado
import com.lajara.blopps.ui.theme.BloppsMoradoClaro
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun DetallePublicacionScreen(
    id: Int,
    sesion: Sesion,
    viewModel: DetalleViewModel,
    resenasViewModel: ResenasViewModel,
    onAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val estadoResenas by resenasViewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(id) {
        viewModel.cargar(id)
        resenasViewModel.cargar(id)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val actual = estado) {
            is UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(
                mensaje = actual.mensaje,
                onReintentar = viewModel::reintentar
            )
            is UiState.Vacio -> ErrorView(
                mensaje = "Esta reseña ya no está disponible",
                onReintentar = viewModel::reintentar
            )
            is UiState.Exito -> ContenidoDetalle(
                publicacion = actual.data,
                onAlternarFavorita = viewModel::alternarFavorita,
                seccionResenas = {
                    ResenasSeccion(
                        sesion = sesion,
                        estado = estadoResenas,
                        onPublicar = { texto, calificacion, alExito ->
                            resenasViewModel.publicar(sesion, texto, calificacion, alExito)
                        },
                        onEliminar = resenasViewModel::eliminar,
                        onLimpiarMensaje = resenasViewModel::limpiarMensaje
                    )
                }
            )
        }
        BarraSuperior(
            favorita = (estado as? UiState.Exito)?.data?.esFavorita,
            onAtras = onAtras,
            onFavorita = viewModel::alternarFavorita
        )
    }
}

@Composable
private fun ContenidoDetalle(
    publicacion: Publicacion,
    onAlternarFavorita: () -> Unit,
    seccionResenas: @Composable () -> Unit
) {
    val pelicula = publicacion.pelicula
    val titulo = (pelicula?.titulo ?: publicacion.titulo).replaceFirstChar { it.uppercase() }
    val resena = publicacion.cuerpo.replace("\n", " ").replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Cabecera(titulo = titulo, posterUrl = pelicula?.posterUrl)
        DatosPelicula(comunidad = publicacion.comunidad, pelicula = pelicula)
        Spacer(Modifier.height(20.dp))
        TarjetaTexto(
            titulo = "Publicación de la comunidad",
            autor = "Usuario ${publicacion.autorId}",
            texto = resena
        )
        if (pelicula != null && pelicula.sinopsis.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            TarjetaTexto(titulo = "Sinopsis", autor = null, texto = pelicula.sinopsis)
        }
        Spacer(Modifier.height(20.dp))
        BotonFavorito(favorita = publicacion.esFavorita, onClick = onAlternarFavorita)
        Spacer(Modifier.height(24.dp))
        seccionResenas()
        if (pelicula != null) {
            Atribucion()
        }
        Spacer(Modifier.navigationBarsPadding().height(24.dp))
    }
}

@Composable
private fun Cabecera(titulo: String, posterUrl: String?) {
    val estiloTitulo = if (titulo.length > 28) {
        MaterialTheme.typography.headlineMedium
    } else {
        MaterialTheme.typography.displaySmall
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        PosterRelleno(posterUrl)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.35f),
                        0.25f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to MaterialTheme.colorScheme.background
                    )
                )
        )
        Text(
            text = titulo,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            style = estiloTitulo,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PosterRelleno(posterUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0E7490), Color(0xFF4C1D95), Color(0xFFA21CAF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Movie,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.18f),
            modifier = Modifier.size(120.dp)
        )
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun BarraSuperior(
    favorita: Boolean?,
    onAtras: () -> Unit,
    onFavorita: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BotonCircular(onClick = onAtras) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
        }
        if (favorita != null) {
            BotonCircular(onClick = onFavorita) {
                Icon(
                    imageVector = if (favorita) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (favorita) {
                        "Quitar de favoritos"
                    } else {
                        "Marcar como favorita"
                    },
                    tint = if (favorita) MaterialTheme.colorScheme.primary else Color.White
                )
            }
        }
    }
}

@Composable
private fun BotonCircular(
    onClick: () -> Unit,
    contenido: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color(0xFF0E0B1A).copy(alpha = 0.65f),
            contentColor = Color.White
        )
    ) {
        contenido()
    }
}

@Composable
private fun DatosPelicula(comunidad: Comunidad, pelicula: PeliculaInfo?) {
    val calificacion = pelicula?.calificacion?.takeIf { it > 0 }
    val anio = pelicula?.anio
    val director = pelicula?.director?.takeIf { it.isNotBlank() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BloppsMorado.copy(alpha = 0.22f),
            contentColor = BloppsMoradoClaro
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = iconoDe(comunidad),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = comunidad.titulo,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (calificacion != null) {
            Dato(Icons.Filled.Star, "%.1f".format(calificacion), BloppsDorado)
        }
        if (anio != null) {
            if (calificacion != null) Separador()
            Dato(
                Icons.Filled.CalendarMonth,
                anio.toString(),
                MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (director != null) {
            if (calificacion != null || anio != null) Separador()
            Dato(Icons.Filled.Person, director, MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Dato(icono: ImageVector, texto: String, tinte: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(20.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )
    }
}

@Composable
private fun Separador() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(18.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun TarjetaTexto(titulo: String, autor: String?, texto: String) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (autor != null) {
                Text(
                    text = buildAnnotatedString {
                        append("Por ")
                        withStyle(
                            SpanStyle(
                                color = BloppsMoradoClaro,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) { append(autor) }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BotonFavorito(favorita: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Icon(Icons.Filled.FavoriteBorder, contentDescription = null)
        Spacer(Modifier.width(14.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(22.dp)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = if (favorita) "Quitar de favoritos" else "Agregar a favoritos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Atribucion() {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "Datos de películas: TMDB. No avalado ni certificado por TMDB.",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun iconoDe(comunidad: Comunidad): ImageVector = when (comunidad) {
    Comunidad.ACCION -> Icons.Filled.LocalFireDepartment
    Comunidad.COMEDIA -> Icons.Filled.Mood
    Comunidad.CIENCIA_FICCION -> Icons.Filled.RocketLaunch
}