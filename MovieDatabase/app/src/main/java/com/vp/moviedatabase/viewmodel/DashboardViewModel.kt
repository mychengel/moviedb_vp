package com.vp.moviedatabase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vp.moviedatabase.data.remote.ServiceLocator
import com.vp.moviedatabase.data.repository.MovieRepository
import com.vp.moviedatabase.model.Movie
import com.vp.moviedatabase.model.MovieCategory
import com.vp.moviedatabase.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Kondisi bagian carousel di atas. */
sealed interface FeaturedState {
    data object Loading : FeaturedState
    data class Success(val movies: List<Movie>) : FeaturedState
    data class Error(val message: String) : FeaturedState
}

/** Seluruh kondisi layar Dashboard dalam satu objek. */
data class DashboardUiState(
    val category: MovieCategory = MovieCategory.POPULAR,
    val featured: FeaturedState = FeaturedState.Loading,
    val movies: List<Movie> = emptyList(),   // list "All Movies"
    val isLoadingList: Boolean = false,      // sedang memuat halaman (pertama / berikutnya)
    val endReached: Boolean = false,
    val listError: String? = null,
)

class DashboardViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var featuredJob: Job? = null
    private var nextPage = 1

    init {
        loadFeatured()
        loadNextPage()
    }

    fun onCategoryChange(category: MovieCategory) {
        if (category == _uiState.value.category) return
        _uiState.update { it.copy(category = category) }
        loadFeatured()
    }

    fun loadFeatured() {
        featuredJob?.cancel() // batalkan request lama bila user berganti kategori cepat
        _uiState.update { it.copy(featured = FeaturedState.Loading) }
        featuredJob = viewModelScope.launch {
            repository.getFeatured(_uiState.value.category)
                .onSuccess { page -> _uiState.update { it.copy(featured = FeaturedState.Success(page.movies)) } }
                .onFailure { e -> _uiState.update { it.copy(featured = FeaturedState.Error(e.toUserMessage())) } }
        }
    }

    /** Dipanggil saat user mendekati akhir list (infinite scroll) atau menekan Retry. */
    fun loadNextPage() {
        val s = _uiState.value
        if (s.isLoadingList || s.endReached || s.listError != null) return

        _uiState.update { it.copy(isLoadingList = true) }
        viewModelScope.launch {
            repository.getAllMovies(nextPage)
                .onSuccess { page ->
                    nextPage = page.page + 1
                    _uiState.update {
                        it.copy(
                            movies = (it.movies + page.movies).distinctBy { m -> m.id },
                            isLoadingList = false,
                            endReached = page.isLastPage,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoadingList = false, listError = e.toUserMessage()) }
                }
        }
    }

    fun retryList() {
        _uiState.update { it.copy(listError = null) }
        loadNextPage()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { DashboardViewModel(ServiceLocator.movieRepository) }
        }
    }
}