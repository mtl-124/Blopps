package com.lajara.blopps.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lajara.blopps.domain.model.Comunidad

class NotificadorAndroid(private val context: Context) : Notificador {

    init {
        crearCanal()
    }

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Sincronización",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos cuando se actualiza el catálogo de películas"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    private fun tienePermiso(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    override fun notificarSincronizacion(cantidad: Int, comunidad: Comunidad) {
        if (!tienePermiso()) return
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Blopps")
            .setContentText("Se actualizaron $cantidad películas de la comunidad ${comunidad.titulo}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICACION_ID, notificacion)
    }

    private companion object {
        const val CANAL_ID = "sincronizacion"
        const val NOTIFICACION_ID = 1001
    }
}