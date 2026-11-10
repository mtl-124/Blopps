package com.lajara.blopps.data.remote

import com.lajara.blopps.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClient {

    private const val URL_BASE_PUBLICACIONES = "https://jsonplaceholder.typicode.com/"
    private const val URL_BASE_PELICULAS = "https://api.themoviedb.org/3/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val clienteHttp: OkHttpClient by lazy {
        val registro = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        OkHttpClient.Builder()
            .addInterceptor(registro)
            .build()
    }

    private val clienteTmdb: OkHttpClient by lazy {
        val parametros = Interceptor { cadena ->
            val original = cadena.request()
            val url = original.url.newBuilder()
                .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
                .addQueryParameter("language", "es-ES")
                .build()
            cadena.proceed(original.newBuilder().url(url).build())
        }
        clienteHttp.newBuilder()
            .addInterceptor(parametros)
            .build()
    }

    private fun crearRetrofit(urlBase: String, cliente: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(urlBase)
            .client(cliente)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val publicaciones: PublicacionApiService by lazy {
        crearRetrofit(URL_BASE_PUBLICACIONES, clienteHttp)
            .create(PublicacionApiService::class.java)
    }

    val peliculas: MovieApiService? by lazy {
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            null
        } else {
            crearRetrofit(URL_BASE_PELICULAS, clienteTmdb).create(MovieApiService::class.java)
        }
    }
}