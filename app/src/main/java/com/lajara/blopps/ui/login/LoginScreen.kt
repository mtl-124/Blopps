package com.lajara.blopps.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lajara.blopps.BuildConfig
import com.lajara.blopps.ui.components.BloppsWordmark
import com.lajara.blopps.ui.theme.BloppsDorado
import com.lajara.blopps.ui.theme.BloppsFondo
import com.lajara.blopps.ui.theme.BloppsMorado
import com.lajara.blopps.ui.theme.BloppsMoradoClaro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: LoginViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var verContrasena by rememberSaveable { mutableStateOf(false) }
    val foco = LocalFocusManager.current

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .drawBehind {
                    tiraDeCine(
                        centro = Offset(size.width * 1.05f, size.height * 0.02f),
                        radio = size.width * 0.8f,
                        inicio = 80f,
                        barrido = 120f
                    )
                    tiraDeCine(
                        centro = Offset(-size.width * 0.15f, size.height * 0.30f),
                        radio = size.width * 0.55f,
                        inicio = -80f,
                        barrido = 120f
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    BloppsWordmark(tamano = 34.sp)
                }
                Spacer(Modifier.height(8.dp))
                AvatarHero()

                Text(
                    text = if (estado.modoRegistro) "Crea tu cuenta" else "Bienvenido",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (estado.modoRegistro) {
                        "Regístrate para escribir tus propias reseñas"
                    } else {
                        "Inicia sesión para ver las reseñas de tu comunidad"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val opciones = listOf("Iniciar sesión", "Registrarse")
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            opciones.forEachIndexed { indice, texto ->
                                val seleccionado = (indice == 1) == estado.modoRegistro
                                SegmentedButton(
                                    selected = seleccionado,
                                    onClick = {
                                        if (!seleccionado && !estado.cargando) viewModel.cambiarModo()
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = indice,
                                        count = opciones.size
                                    ),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = BloppsMorado,
                                        activeContentColor = Color.White,
                                        activeBorderColor = BloppsMorado,
                                        inactiveContainerColor = Color.Transparent,
                                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        inactiveBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    icon = {},
                                    label = { Text(texto, fontWeight = FontWeight.SemiBold) }
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))

                        CampoLogin(
                            valor = estado.correo,
                            onCambio = viewModel::onCorreoChange,
                            textoGuia = "Correo electrónico",
                            icono = Icons.Filled.Email,
                            error = estado.errorCorreo,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { foco.moveFocus(FocusDirection.Down) }
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        CampoLogin(
                            valor = estado.contrasena,
                            onCambio = viewModel::onContrasenaChange,
                            textoGuia = "Contraseña",
                            icono = Icons.Filled.Lock,
                            error = estado.errorContrasena,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (estado.modoRegistro) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { foco.moveFocus(FocusDirection.Down) },
                                onDone = {
                                    foco.clearFocus()
                                    viewModel.enviar()
                                }
                            ),
                            visualTransformation = if (verContrasena) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            accionFinal = {
                                IconButton(onClick = { verContrasena = !verContrasena }) {
                                    Icon(
                                        imageVector = if (verContrasena) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = if (verContrasena) {
                                            "Ocultar contraseña"
                                        } else {
                                            "Mostrar contraseña"
                                        }
                                    )
                                }
                            }
                        )

                        if (estado.modoRegistro) {
                            Spacer(Modifier.height(8.dp))
                            CampoLogin(
                                valor = estado.confirmacion,
                                onCambio = viewModel::onConfirmacionChange,
                                textoGuia = "Confirmar contraseña",
                                icono = Icons.Filled.Lock,
                                error = estado.errorConfirmacion,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        foco.clearFocus()
                                        viewModel.enviar()
                                    }
                                ),
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }

                        estado.errorGeneral?.let { mensaje ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                foco.clearFocus()
                                viewModel.enviar()
                            },
                            enabled = !estado.cargando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            if (estado.cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (estado.modoRegistro) {
                                        Icons.Filled.PersonAdd
                                    } else {
                                        Icons.AutoMirrored.Filled.Login
                                    },
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = if (estado.modoRegistro) "Crear cuenta" else "Iniciar sesión",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                FranjaBeneficios()
                Spacer(Modifier.height(16.dp))
                PastillaVersion()
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CampoLogin(
    valor: String,
    onCambio: (String) -> Unit,
    textoGuia: String,
    icono: ImageVector,
    error: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    accionFinal: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(textoGuia) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = accionFinal,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else {
            null
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLeadingIconColor = BloppsMoradoClaro,
            unfocusedLeadingIconColor = BloppsMoradoClaro
        )
    )
}

@Composable
private fun AvatarHero() {
    Box(modifier = Modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(210.dp)
                .background(
                    Brush.radialGradient(
                        listOf(BloppsMorado.copy(alpha = 0.45f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(132.dp)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(listOf(Color.White, BloppsDorado)),
                    shape = CircleShape
                )
                .padding(6.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF5B21B6), BloppsMorado))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Movie,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 36.dp, bottom = 34.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(BloppsMorado),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Destello(
            tamano = 28.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 48.dp)
        )
        Destello(
            tamano = 38.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 4.dp, top = 70.dp)
        )
    }
}

@Composable
private fun FranjaBeneficios() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Beneficio(Icons.Filled.RateReview, "Escribe", "Reseñas", Modifier.weight(1f))
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.outline
            )
            Beneficio(Icons.Filled.Favorite, "Guarda", "Favoritas", Modifier.weight(1f))
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.outline
            )
            Beneficio(Icons.Filled.Groups, "Únete", "Comunidades", Modifier.weight(1f))
        }
    }
}

@Composable
private fun Beneficio(
    icono: ImageVector,
    titulo: String,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = BloppsMorado,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PastillaVersion() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Blopps v${BuildConfig.VERSION_NAME} • Reseñas de películas, comunidad en un solo lugar",
                style = MaterialTheme.typography.labelMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Destello(tamano: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(tamano)) {
        val c = center
        val r = size.minDimension / 2f
        val k = r * 0.18f
        val estrella = Path().apply {
            moveTo(c.x, c.y - r)
            quadraticTo(c.x + k, c.y - k, c.x + r, c.y)
            quadraticTo(c.x + k, c.y + k, c.x, c.y + r)
            quadraticTo(c.x - k, c.y + k, c.x - r, c.y)
            quadraticTo(c.x - k, c.y - k, c.x, c.y - r)
            close()
        }
        drawPath(path = estrella, color = BloppsDorado)
    }
}

private fun DrawScope.tiraDeCine(centro: Offset, radio: Float, inicio: Float, barrido: Float) {
    val ancho = 56.dp.toPx()
    drawArc(
        color = BloppsMorado.copy(alpha = 0.16f),
        startAngle = inicio,
        sweepAngle = barrido,
        useCenter = false,
        topLeft = Offset(centro.x - radio, centro.y - radio),
        size = Size(radio * 2f, radio * 2f),
        style = Stroke(width = ancho)
    )
    val hueco = 8.dp.toPx()
    for (desfase in listOf(-ancho * 0.32f, ancho * 0.32f)) {
        val r = radio + desfase
        drawArc(
            color = BloppsFondo.copy(alpha = 0.55f),
            startAngle = inicio,
            sweepAngle = barrido,
            useCenter = false,
            topLeft = Offset(centro.x - r, centro.y - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(
                width = hueco,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(hueco, hueco * 1.4f))
            )
        )
    }
}