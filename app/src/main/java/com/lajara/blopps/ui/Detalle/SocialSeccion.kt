package com.lajara.blopps.ui.detalle

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lajara.blopps.domain.model.Comentario
import com.lajara.blopps.domain.model.LIMITE_COMENTARIO
import com.lajara.blopps.domain.model.Rol
import com.lajara.blopps.domain.model.Sesion
import com.lajara.blopps.ui.components.CalificacionEstrellas
import com.lajara.blopps.ui.components.ErrorView
import com.lajara.blopps.ui.util.fechaRelativa
import kotlin.math.roundToInt

@Composable
fun SocialSeccion(
    publicacionId: String,
    sesion: Sesion,
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var limiteHilos by remember { mutableIntStateOf(50) }

    LaunchedEffect(publicacionId, sesion.uid) {
        viewModel.cargar(publicacionId, sesion.uid)
    }

    if (estado.errorCarga != null) {
        ErrorView(mensaje = estado.errorCarga!!, onReintentar = { viewModel.cargar(publicacionId, sesion.uid) })
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Fila de acciones (Me gusta)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconToggleButton(
                checked = estado.resumen.yoDiLike,
                onCheckedChange = { viewModel.alternarLike(publicacionId, sesion.uid) }
            ) {
                Icon(
                    imageVector = if (estado.resumen.yoDiLike) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Me gusta",
                    tint = if (estado.resumen.yoDiLike) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${estado.resumen.totalLikes} ${if (estado.resumen.totalLikes == 1) "me gusta" else "me gusta"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bloque de calificación promedio
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "%.1f".format(estado.resumen.promedio),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        CalificacionEstrellas(
                            valor = estado.resumen.promedio.roundToInt(),
                            onCambio = null,
                            tamano = 20.dp
                        )
                        Spacer(Modifier.height(2.dp))
                        val votosTexto = if (estado.resumen.totalVotos == 1) "1 voto" else "${estado.resumen.totalVotos} votos"
                        Text(
                            text = if (estado.resumen.totalVotos > 0) votosTexto else "Sé el primero en calificar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text("Tu voto:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                CalificacionEstrellas(
                    valor = estado.resumen.miVoto ?: 0,
                    onCambio = { valor -> viewModel.votar(publicacionId, sesion.uid, valor) },
                    tamano = 32.dp
                )
            }
        }

        // Sección Comentarios
        Text(
            text = "Comentarios (${estado.totalComentarios})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Campo de entrada de comentario
        Column(modifier = Modifier.fillMaxWidth()) {
            if (estado.respondiendoA != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Respondiendo a ${estado.respondiendoA!!.autorCorreo.substringBefore("@")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = viewModel::cancelarRespuesta, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancelar respuesta")
                    }
                }
            }

            OutlinedTextField(
                value = estado.texto,
                onValueChange = viewModel::cambiarTexto,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Escribe un comentario...") },
                supportingText = {
                    Text("${estado.texto.length}/$LIMITE_COMENTARIO")
                },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.enviarComentario(sesion) },
                        enabled = estado.texto.isNotBlank() && !estado.enviando
                    ) {
                        if (estado.enviando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar comentario")
                        }
                    }
                },
                shape = RoundedCornerShape(
                    topStart = if (estado.respondiendoA != null) 0.dp else 16.dp,
                    topEnd = if (estado.respondiendoA != null) 0.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                minLines = 2,
                maxLines = 4
            )
        }

        // Lista de hilos
        if (estado.hilos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay comentarios. ¡Escribe el primero!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                estado.hilos.take(limiteHilos).forEach { hilo ->
                    ComentarioItem(
                        comentario = hilo.raiz,
                        esRaiz = true,
                        sesion = sesion,
                        onResponder = { viewModel.responderA(hilo.raiz) },
                        onEliminar = { id -> viewModel.eliminarComentario(publicacionId, id) }
                    )
                    hilo.respuestas.forEach { respuesta ->
                        ComentarioItem(
                            comentario = respuesta,
                            esRaiz = false,
                            sesion = sesion,
                            onResponder = {},
                            onEliminar = { id -> viewModel.eliminarComentario(publicacionId, id) }
                        )
                    }
                }

                if (estado.hilos.size > limiteHilos) {
                    TextButton(
                        onClick = { limiteHilos += 50 },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Ver más comentarios")
                    }
                }
            }
        }
    }
}

@Composable
private fun ComentarioItem(
    comentario: Comentario,
    esRaiz: Boolean,
    sesion: Sesion,
    onResponder: () -> Unit,
    onEliminar: (String) -> Unit
) {
    val autorNombre = comentario.autorCorreo.substringBefore("@")
    val tiempo = fechaRelativa(System.currentTimeMillis(), comentario.fecha)
    val puedeBorrar = sesion.uid == comentario.autorUid || sesion.rol == Rol.ADMIN

    val modifier = if (!esRaiz) {
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (esRaiz) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = autorNombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = tiempo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = comentario.texto,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (esRaiz) {
                    TextButton(onClick = onResponder) {
                        Text("Responder", style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (puedeBorrar) {
                    IconButton(onClick = { onEliminar(comentario.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar comentario",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
