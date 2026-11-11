package com.lajara.blopps.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lajara.blopps.AppContainer
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.Rol
import com.lajara.blopps.domain.model.Sesion
import com.lajara.blopps.ui.admin.AdminScreen
import com.lajara.blopps.ui.admin.AdminViewModel
import com.lajara.blopps.ui.comunidades.ComunidadesScreen
import com.lajara.blopps.ui.comunidades.PublicacionesViewModel
import com.lajara.blopps.ui.components.CargandoView
import com.lajara.blopps.ui.crear.CrearPublicacionScreen
import com.lajara.blopps.ui.crear.CrearPublicacionViewModel
import com.lajara.blopps.ui.detalle.DetallePublicacionUsuarioScreen
import com.lajara.blopps.ui.detalle.DetallePublicacionUsuarioViewModel
import com.lajara.blopps.ui.detalle.DetallePublicacionScreen
import com.lajara.blopps.ui.detalle.DetalleViewModel
import com.lajara.blopps.ui.detalle.ResenasViewModel
import com.lajara.blopps.ui.favoritos.FavoritosScreen
import com.lajara.blopps.ui.favoritos.FavoritosViewModel
import com.lajara.blopps.ui.login.LoginScreen
import com.lajara.blopps.ui.login.LoginViewModel
import com.lajara.blopps.ui.perfil.PerfilScreen
import com.lajara.blopps.ui.perfil.PerfilViewModel
import com.lajara.blopps.ui.theme.BloppsMorado
import com.lajara.blopps.ui.theme.BloppsMoradoClaro

object Rutas {
    const val INICIO = "inicio"
    const val FAVORITOS = "favoritos"
    const val PANEL = "panel"
    const val PERFIL = "perfil"
    const val DETALLE = "detalle/{id}"
    const val CREAR = "crear?comunidad={comunidad}"
    const val DETALLE_USUARIO = "usuario/{id}"

    fun detalle(id: Int): String = "detalle/$id"
    fun crear(comunidad: Comunidad? = null): String =
        if (comunidad != null) "crear?comunidad=${comunidad.name}" else "crear"
    fun detalleUsuario(id: String): String = "usuario/$id"
}

private data class DestinoBarra(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

@Composable
fun AppNav(container: AppContainer) {
    val loginViewModel: LoginViewModel = viewModel(factory = container.viewModelFactory)
    val estado by loginViewModel.uiState.collectAsStateWithLifecycle()
    val sesion = estado.sesion

    when {
        estado.verificandoSesion -> CargandoView()
        sesion == null -> LoginScreen(loginViewModel)
        else -> PrincipalScreen(
            container = container,
            sesion = sesion,
            onCerrarSesion = loginViewModel::cerrarSesion
        )
    }
}

@Composable
private fun PrincipalScreen(
    container: AppContainer,
    sesion: Sesion,
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()
    val destinos = remember(sesion.rol) {
        buildList {
            add(DestinoBarra(Rutas.INICIO, "Inicio", Icons.Filled.Home))
            add(DestinoBarra(Rutas.FAVORITOS, "Favoritos", Icons.Filled.Star))
            if (sesion.rol == Rol.ADMIN) {
                add(DestinoBarra(Rutas.PANEL, "Panel", Icons.Filled.BarChart))
            }
            add(DestinoBarra(Rutas.PERFIL, "Perfil", Icons.Filled.Person))
        }
    }
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    val irAPestana: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    val abrirDetalle: (Int) -> Unit = { id ->
        navController.navigate(Rutas.detalle(id)) { launchSingleTop = true }
    }
    val abrirDetalleUsuario: (String) -> Unit = { id ->
        navController.navigate(Rutas.detalleUsuario(id)) { launchSingleTop = true }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (rutaActual != Rutas.DETALLE && !rutaActual.orEmpty().startsWith("crear") && !rutaActual.orEmpty().startsWith("usuario")) {
                BarraInferior(
                    destinos = destinos,
                    rutaActual = rutaActual,
                    onSeleccionar = irAPestana
                )
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = Rutas.INICIO,
            modifier = Modifier.padding(relleno)
        ) {
            composable(Rutas.INICIO) {
                val viewModel: PublicacionesViewModel =
                    viewModel(factory = container.viewModelFactory)
                ComunidadesScreen(
                    viewModel = viewModel,
                    onAbrirDetalle = abrirDetalle,
                    onAbrirDetalleUsuario = abrirDetalleUsuario,
                    onCrear = { comunidad -> navController.navigate(Rutas.crear(comunidad)) }
                )
            }
            composable(
                route = Rutas.CREAR,
                arguments = listOf(navArgument("comunidad") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { entry ->
                val comunidadStr = entry.arguments?.getString("comunidad")
                val comunidadInicial = comunidadStr?.let { s -> Comunidad.entries.firstOrNull { it.name == s } }
                val viewModel: CrearPublicacionViewModel =
                    viewModel(factory = container.viewModelFactory)
                CrearPublicacionScreen(
                    sesion = sesion,
                    viewModel = viewModel,
                    comunidadInicial = comunidadInicial,
                    onAtras = { navController.popBackStack() }
                )
            }
            composable(
                route = Rutas.DETALLE_USUARIO,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val viewModel: DetallePublicacionUsuarioViewModel =
                    viewModel(factory = container.viewModelFactory)
                DetallePublicacionUsuarioScreen(
                    sesion = sesion,
                    id = id,
                    viewModel = viewModel,
                    onAtras = { navController.popBackStack() }
                )
            }
            composable(
                route = Rutas.DETALLE,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { argumentos ->
                val id = argumentos.arguments?.getInt("id") ?: 0
                val viewModel: DetalleViewModel =
                    viewModel(factory = container.viewModelFactory)
                val resenasViewModel: ResenasViewModel =
                    viewModel(factory = container.viewModelFactory)
                DetallePublicacionScreen(
                    id = id,
                    sesion = sesion,
                    viewModel = viewModel,
                    resenasViewModel = resenasViewModel,
                    onAtras = { navController.popBackStack() }
                )
            }
            composable(Rutas.FAVORITOS) {
                val viewModel: FavoritosViewModel =
                    viewModel(factory = container.viewModelFactory)
                FavoritosScreen(viewModel = viewModel, onAbrirDetalle = abrirDetalle)
            }
            composable(Rutas.PANEL) {
                if (sesion.rol == Rol.ADMIN) {
                    val viewModel: AdminViewModel =
                        viewModel(factory = container.viewModelFactory)
                    AdminScreen(viewModel = viewModel)
                }
            }
            composable(Rutas.PERFIL) {
                val viewModel: PerfilViewModel =
                    viewModel(factory = container.viewModelFactory)
                PerfilScreen(
                    sesion = sesion,
                    viewModel = viewModel,
                    onCerrarSesion = onCerrarSesion,
                    onVerFavoritos = { irAPestana(Rutas.FAVORITOS) }
                )
            }
        }
    }
}

@Composable
private fun BarraInferior(
    destinos: List<DestinoBarra>,
    rutaActual: String?,
    onSeleccionar: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                destinos.forEach { destino ->
                    val seleccionado = rutaActual == destino.ruta
                    NavigationBarItem(
                        selected = seleccionado,
                        onClick = { onSeleccionar(destino.ruta) },
                        icon = { Icon(destino.icono, contentDescription = null) },
                        label = { Text(destino.titulo, fontWeight = FontWeight.SemiBold) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BloppsMoradoClaro,
                            selectedTextColor = BloppsMoradoClaro,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.drawBehind {
                            if (seleccionado) {
                                drawRoundRect(
                                    color = BloppsMorado,
                                    topLeft = Offset(size.width * 0.2f, size.height - 6.dp.toPx()),
                                    size = Size(size.width * 0.6f, 3.dp.toPx()),
                                    cornerRadius = CornerRadius(2.dp.toPx())
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
