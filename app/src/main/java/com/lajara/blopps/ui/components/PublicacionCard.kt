package com.lajara.blopps.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lajara.blopps.domain.model.Publicacion
import com.lajara.blopps.ui.theme.BloppsDorado
import com.lajara.blopps.ui.theme.BloppsMoradoClaro

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun PublicacionCard(
    publicacion: Publicacion,
    onClick: () -> Unit,
    onFavorita: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pelicula = publicacion.pelicula
    val titulo = (pelicula?.titulo ?: publicacion.titulo).replaceFirstChar { it.uppercase() }
    val descripcion = when {
        pelicula == null -> publicacion.cuerpo.replace("\n", " ")
        pelicula.sinopsis.isNotBlank() -> pelicula.sinopsis.replace("\n", " ")
        else -> "Sinopsis no disponible"
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PosterRelleno(posterUrl = pelicula?.posterUrl)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = titulo,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = onFavorita, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (publicacion.esFavorita) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = if (publicacion.esFavorita) {
                                "Quitar de favoritos"
                            } else {
                                "Marcar como favorita"
                            },
                            tint = if (publicacion.esFavorita) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pelicula != null && pelicula.calificacion > 0) {
                        Dato(Icons.Filled.Star, "%.1f".format(pelicula.calificacion), BloppsDorado)
                        Separador()
                    }
                    pelicula?.anio?.let { anio ->
                        Dato(
                            Icons.Filled.CalendarMonth,
                            anio.toString(),
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Separador()
                    }
                    Dato(
                        icono = Icons.Filled.Person,
                        texto = "Por Usuario ${publicacion.autorId}",
                        tinte = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterRelleno(posterUrl: String?) {
    Box(
        modifier = Modifier
            .width(96.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF3B2A7A), Color(0xFF14102B)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Movie,
            contentDescription = null,
            tint = BloppsMoradoClaro.copy(alpha = 0.7f),
            modifier = Modifier.size(36.dp)
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
private fun Dato(
    icono: ImageVector,
    texto: String,
    tinte: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(16.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Separador() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(14.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}