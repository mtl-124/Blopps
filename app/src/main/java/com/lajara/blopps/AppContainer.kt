package com.lajara.blopps

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.lajara.blopps.data.auth.AuthRepository
import com.lajara.blopps.data.auth.FirebaseAuthRepository
import com.lajara.blopps.data.local.AppDatabase
import com.lajara.blopps.data.remote.RetrofitClient
import com.lajara.blopps.data.repository.FirestorePublicacionUsuarioRepository
import com.lajara.blopps.data.repository.FirestoreResenaRepository
import com.lajara.blopps.data.repository.PeliculaRepository
import com.lajara.blopps.data.repository.PeliculaRepositoryImpl
import com.lajara.blopps.data.repository.PublicacionRepository
import com.lajara.blopps.data.repository.PublicacionRepositoryImpl
import com.lajara.blopps.data.repository.PublicacionUsuarioRepository
import com.lajara.blopps.data.repository.ResenaRepository
import com.lajara.blopps.notification.Notificador
import com.lajara.blopps.notification.NotificadorAndroid
import com.lajara.blopps.ui.admin.AdminViewModel
import com.lajara.blopps.ui.comunidades.PublicacionesViewModel
import com.lajara.blopps.ui.crear.CrearPublicacionViewModel
import com.lajara.blopps.ui.detalle.DetallePublicacionUsuarioViewModel
import com.lajara.blopps.ui.detalle.DetalleViewModel
import com.lajara.blopps.ui.detalle.ResenasViewModel
import com.lajara.blopps.ui.favoritos.FavoritosViewModel
import com.lajara.blopps.ui.login.LoginViewModel
import com.lajara.blopps.ui.perfil.PerfilViewModel

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }

    val notificador: Notificador by lazy { NotificadorAndroid(appContext) }

    private val baseDeDatos: AppDatabase by lazy { AppDatabase.crear(appContext) }

    val publicacionRepository: PublicacionRepository by lazy {
        PublicacionRepositoryImpl(
            api = RetrofitClient.publicaciones,
            peliculasApi = RetrofitClient.peliculas,
            dao = baseDeDatos.publicacionDao(),
            uidActual = { FirebaseAuth.getInstance().currentUser?.uid }
        )
    }

    val peliculaRepository: PeliculaRepository by lazy {
        PeliculaRepositoryImpl(RetrofitClient.peliculas)
    }

    val publicacionUsuarioRepository: PublicacionUsuarioRepository by lazy {
        FirestorePublicacionUsuarioRepository()
    }

    val resenaRepository: ResenaRepository by lazy { FirestoreResenaRepository() }

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { LoginViewModel(authRepository) }
        initializer { PublicacionesViewModel(publicacionRepository, publicacionUsuarioRepository, notificador) }
        initializer { DetalleViewModel(publicacionRepository) }
        initializer { ResenasViewModel(resenaRepository) }
        initializer { FavoritosViewModel(publicacionRepository) }
        initializer { AdminViewModel(publicacionRepository, notificador) }
        initializer { PerfilViewModel(authRepository, publicacionRepository) }
        initializer { CrearPublicacionViewModel(peliculaRepository, publicacionUsuarioRepository) }
        initializer { DetallePublicacionUsuarioViewModel(publicacionUsuarioRepository) }
    }
}
