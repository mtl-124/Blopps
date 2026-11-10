package com.lajara.blopps.ui.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.blopps.data.repository.PublicacionRepository
import com.lajara.blopps.domain.model.Publicacion
import com.lajara.blopps.ui.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritosViewModel(private val repo: PublicacionRepository) : ViewModel() {

    val estado: StateFlow<UiState<List<Publicacion>>> = repo.favoritas
        .map<List<Publicacion>, UiState<List<Publicacion>>> { lista ->
            if (lista.isEmpty()) UiState.Vacio else UiState.Exito(lista)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Cargando)

    fun quitar(id: Int) {
        viewModelScope.launch { repo.alternarFavorita(id) }
    }
}