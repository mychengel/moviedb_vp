package com.vp.moviedatabase.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.vp.moviedatabase.data.repository.MovieRepository
import com.vp.moviedatabase.data.remote.ServiceLocator
import com.vp.moviedatabase.model.MovieDetail
import com.vp.moviedatabase.navigation.DetailRoute
import com.vp.moviedatabase.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val movie: MovieDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
) : ViewModel() {

    // Argumen route dibaca di ViewModel; layar hanya mengirim movieId (bukan seluruh objek)
    private val movieId: Int = savedStateHandle.toRoute<DetailRoute>().movieId

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            repository.getMovieDetail(movieId)
                .onSuccess { _uiState.value = DetailUiState.Success(it) }
                .onFailure { _uiState.value = DetailUiState.Error(it.toUserMessage()) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                DetailViewModel(createSavedStateHandle(), ServiceLocator.movieRepository)
            }
        }
    }
}