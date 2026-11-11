package com.lajara.blopps.ui.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lajara.blopps.domain.model.Rol
import com.lajara.blopps.domain.model.Sesion
import com.lajara.blopps.ui.theme.BloppsDorado
import com.lajara.blopps.ui.theme.BloppsError
import com.lajara.blopps.ui.theme.BloppsMorado
import com.lajara.blopps.ui.theme.BloppsMoradoClaro

@Composable
fun PerfilScreen(
    sesion: Sesion,
    viewModel: PerfilViewModel,
    onCerrarSesion: () -> Unit,
    onVerFavoritos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    val esAdmin = sesion.rol == Rol.ADMIN
    val nombre = sesion.correo.substringBefore("@").replaceFirstChar { it.uppercase() }

    LaunchedEffect(Unit) { viewModel.actualizarGuardadas() }
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Perfil",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(24.dp))
            Avatar(correo = sesion.correo)
            Spacer(Modifier.height(14.dp))
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = sesion.correo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            ChipRol(esAdmin = esAdmin)
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Estadistica(
                    modifier = Modifier.weight(1f),
                    icono = Icons.Filled.Favorite,
                    valor = estado.favoritas.toString(),
                    etiqueta = "Mis favoritos"
                )
                Estadistica(
                    modifier = Modifier.weight(1f),
                    icono = Icons.Filled.Movie,
                    valor = estado.guardadas.toString(),
                    etiqueta = "Reseñas guardadas"
                )
            }
            Spacer(Modifier.height(16.dp))
            TarjetaCuenta(
                sesion = sesion,
                esAdmin = esAdmin,
                onVerFavoritos = onVerFavoritos
            )
            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = { viewModel.restablecerContrasena(sesion.correo) },
                enabled = !estado.enviandoCorreo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                if (estado.enviandoCorreo) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.LockReset, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Restablecer contraseña", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { confirmarSalida = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BloppsError.copy(alpha = 0.16f),
                    contentColor = BloppsError
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tendrás que ingresar tu correo y contraseña otra vez.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarSalida = false
                        onCerrarSesion()
                    }
                ) {
                    Text("Cerrar sesión", color = BloppsError)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun Avatar(correo: String) {
    Box(
        modifier = Modifier
            .size(112.dp)
            .border(3.dp, BloppsDorado, CircleShape)
            .padding(6.dp)
            .background(Brush.linearGradient(listOf(Color(0xFF5B21B6), BloppsMorado)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = correo.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
private fun ChipRol(esAdmin: Boolean) {
    val color = if (esAdmin) BloppsDorado else BloppsMoradoClaro
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.16f),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (esAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (esAdmin) "Administrador" else "Usuario",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun Estadistica(
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
                style = MaterialTheme.typography.headlineSmall,
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
private fun TarjetaCuenta(
    sesion: Sesion,
    esAdmin: Boolean,
    onVerFavoritos: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            FilaInfo(Icons.Filled.Email, "Correo", sesion.correo)
            Divisor()
            FilaInfo(
                icono = if (esAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Person,
                titulo = "Permisos",
                valor = if (esAdmin) {
                    "Catálogo, favoritos y panel de administración"
                } else {
                    "Catálogo y favoritos personales"
                }
            )
            Divisor()
            FilaInfo(Icons.Filled.Lock, "Privacidad", "Tus favoritos son solo de esta cuenta")
            Divisor()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onVerFavoritos)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = BloppsMoradoClaro,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = "Ver mis favoritos",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FilaInfo(icono: ImageVector, titulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = BloppsMoradoClaro,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = valor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Divisor() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
}