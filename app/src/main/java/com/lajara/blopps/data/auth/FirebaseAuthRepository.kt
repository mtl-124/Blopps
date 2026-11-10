package com.lajara.blopps.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.lajara.blopps.domain.model.Rol
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class CuentaEliminadaException : Exception("La cuenta fue eliminada")

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> {
        val usuario = try {
            auth.signInWithEmailAndPassword(correo, clave).await().user
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(ETIQUETA, "Fallo la autenticación", e)
            return Result.failure(e)
        }
        if (usuario == null) {
            return Result.failure(IllegalStateException("Usuario no disponible"))
        }
        val sesion = sesionDe(usuario.uid, usuario.email ?: correo)
        if (sesion == null) {
            auth.signOut()
            return Result.failure(CuentaEliminadaException())
        }
        return Result.success(sesion)
    }

    override suspend fun registrar(correo: String, clave: String): Result<Sesion> {
        val usuario = try {
            auth.createUserWithEmailAndPassword(correo, clave).await().user
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(ETIQUETA, "Fallo el registro", e)
            return Result.failure(e)
        }
        if (usuario == null) {
            return Result.failure(IllegalStateException("Usuario no disponible"))
        }
        return try {
            val datos = mapOf(
                "correo" to correo,
                "rol" to "usuario",
                "creadoEn" to FieldValue.serverTimestamp()
            )
            firestore.collection("usuarios").document(usuario.uid).set(datos).await()
            Result.success(Sesion(uid = usuario.uid, correo = correo, rol = Rol.USUARIO))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(ETIQUETA, "No se pudo guardar el perfil", e)
            runCatching { usuario.delete().await() }
            auth.signOut()
            Result.failure(e)
        }
    }

    override suspend fun sesionActual(): Sesion? {
        val usuario = auth.currentUser ?: return null
        val sesion = sesionDe(usuario.uid, usuario.email.orEmpty())
        if (sesion == null) auth.signOut()
        return sesion
    }

    override suspend fun enviarCorreoRestablecimiento(correo: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(correo).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(ETIQUETA, "No se pudo enviar el correo de restablecimiento", e)
            Result.failure(e)
        }
    }

    override fun cerrarSesion() {
        auth.signOut()
    }

    // Devuelve null si el documento del usuario no existe (cuenta eliminada por un admin).
    private suspend fun sesionDe(uid: String, correo: String): Sesion? {
        return try {
            val documento = firestore.collection("usuarios").document(uid).get().await()
            if (!documento.exists()) {
                null
            } else {
                val rol = if (documento.getString("rol") == "admin") Rol.ADMIN else Rol.USUARIO
                Sesion(uid = uid, correo = correo, rol = rol)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(ETIQUETA, "No se pudo leer el rol de $uid", e)
            Sesion(uid = uid, correo = correo, rol = Rol.USUARIO)
        }
    }

    private companion object {
        const val ETIQUETA = "BloppsAuth"
    }
}