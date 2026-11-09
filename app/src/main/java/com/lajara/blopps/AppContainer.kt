package com.lajara.blopps

import android.content.Context
import com.lajara.blopps.data.auth.AuthRepository
import com.lajara.blopps.data.auth.FirebaseAuthRepository
import com.lajara.blopps.notification.Notificador
import com.lajara.blopps.notification.NotificadorAndroid

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }

    val notificador: Notificador by lazy { NotificadorAndroid(appContext) }
}