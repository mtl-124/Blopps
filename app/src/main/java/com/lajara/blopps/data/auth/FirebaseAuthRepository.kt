package com.lajara.blopps.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lajara.blopps.domain.model.Rol
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun iniciarSesion(correo: String, contrasena: String): Result<Sesion> {
        return try {
            val resultado = auth.signInWithEmailAndPassword(correo, contrasena).await()
            val usuario = resultado.user
                ?: return Result.failure(IllegalStateException("Usuario no disponible"))
            Result.success(construirSesion(usuario.uid, usuario.email ?: correo))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerSesionActual(): Sesion? {
        val usuario = auth.currentUser ?: return null
        val correo = usuario.email.orEmpty()
        return try {
            construirSesion(usuario.uid, correo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Sesion(uid = usuario.uid, correo = correo, rol = Rol.USUARIO)
        }
    }

    override fun cerrarSesion() {
        auth.signOut()
    }

    private suspend fun construirSesion(uid: String, correo: String): Sesion {
        val documento = firestore.collection("usuarios").document(uid).get().await()
        val rol = when (documento.getString("rol")) {
            "admin" -> Rol.ADMIN
            else -> Rol.USUARIO
        }
        return Sesion(uid = uid, correo = correo, rol = rol)
    }
}