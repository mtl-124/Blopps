package com.lajara.blopps

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lajara.blopps.data.auth.AuthRepository
import com.lajara.blopps.data.auth.FirebaseAuthRepository
import com.lajara.blopps.data.local.AppDatabase
import com.lajara.blopps.data.remote.RetrofitClient
import com.lajara.blopps.data.repository.PublicacionRepository
import com.lajara.blopps.data.repository.PublicacionRepositoryImpl
import com.lajara.blopps.notification.Notificador
import com.lajara.blopps.notification.NotificadorAndroid
import com.lajara.blopps.ui.admin.AdminViewModel
import com.lajara.blopps.ui.comunidades.PublicacionesViewModel
import com.lajara.blopps.ui.detalle.DetalleViewModel
import com.lajara.blopps.ui.favoritos.FavoritosViewModel
import com.lajara.blopps.ui.login.LoginViewModel

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }

    val notificador: Notificador by lazy { NotificadorAndroid(appContext) }

    private val baseDeDatos: AppDatabase by lazy { AppDatabase.crear(appContext) }

    val publicacionRepository: PublicacionRepository by lazy {
        PublicacionRepositoryImpl(RetrofitClient.publicaciones, baseDeDatos.publicacionDao())
    }

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { LoginViewModel(authRepository) }
        initializer { PublicacionesViewModel(publicacionRepository, notificador) }
        initializer { DetalleViewModel(publicacionRepository) }
        initializer { FavoritosViewModel(publicacionRepository) }
        initializer { AdminViewModel(publicacionRepository, notificador) }
    }
}