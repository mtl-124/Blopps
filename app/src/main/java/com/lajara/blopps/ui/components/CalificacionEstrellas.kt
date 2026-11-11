package com.lajara.blopps.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lajara.blopps.ui.theme.BloppsDorado

@Composable
fun CalificacionEstrellas(
    valor: Int,
    onCambio: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    tamano: Dp = 32.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val seleccionada = i <= valor
            val scale by animateFloatAsState(
                targetValue = if (seleccionada) 1.25f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "estrella_escala_$i"
            )
            IconButton(
                onClick = { onCambio?.invoke(i) },
                enabled = onCambio != null,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                Icon(
                    imageVector = if (seleccionada) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "$i estrellas",
                    tint = if (seleccionada) BloppsDorado else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(tamano)
                )
            }
        }
    }
}
