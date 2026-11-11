package com.lajara.blopps.ui.favoritos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lajara.blopps.ui.UiState
import com.lajara.blopps.ui.components.CargandoShimmer
import com.lajara.blopps.ui.components.PublicacionCard
import com.lajara.blopps.ui.components.VacioView
import com.lajara.blopps.ui.theme.BloppsDorado

@Composable
fun FavoritosScreen(
    viewModel: FavoritosViewModel,
    onAbrirDetalle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = buildAnnotatedString {
                    append("Mis ")
                    withStyle(SpanStyle(color = BloppsDorado)) { append("favoritos") }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Solo tú ves esta lista en tu cuenta",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = estado,
                transitionSpec = { fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250)) },
                label = "favoritos_anim"
            ) { actual ->
                when (actual) {
                    is UiState.Cargando -> CargandoShimmer()
                    is UiState.Vacio -> VacioView(
                        titulo = "Aún no tienes favoritos",
                        descripcion = "Toca el corazón de una reseña para guardarla aquí",
                        icono = Icons.Filled.FavoriteBorder
                    )
                    is UiState.Error -> VacioView(
                        titulo = actual.mensaje,
                        descripcion = "No se pudieron cargar tus favoritos",
                        icono = Icons.Filled.FavoriteBorder
                    )
                    is UiState.Exito -> LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(actual.data, key = { it.id }) { publicacion ->
                            PublicacionCard(
                                publicacion = publicacion,
                                onClick = { onAbrirDetalle(publicacion.id) },
                                onFavorita = { viewModel.quitar(publicacion.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}
