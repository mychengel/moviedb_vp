package com.vp.moviedatabase.ui.screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vp.moviedatabase.model.MovieCategory
import com.vp.moviedatabase.ui.widgets.ErrorBox
import com.vp.moviedatabase.ui.widgets.LoadingBox
import com.vp.moviedatabase.ui.widgets.MovieCard
import com.vp.moviedatabase.ui.widgets.MovieListItem
import com.vp.moviedatabase.viewmodel.DashboardUiState
import com.vp.moviedatabase.viewmodel.DashboardViewModel
import com.vp.moviedatabase.viewmodel.FeaturedState

/** STATEFUL: tahu ViewModel, mengumpulkan state, meneruskan event. */
@Composable
fun DashboardScreen(
    onMovieClick: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        state = state,
        onMovieClick = onMovieClick,
        onCategoryChange = viewModel::onCategoryChange,
        onRetryFeatured = viewModel::loadFeatured,
        onLoadMore = viewModel::loadNextPage,
        onRetryList = viewModel::retryList,
    )
}

/** STATELESS: hanya menggambar UI dari state -> mudah di-preview & dites. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardUiState,
    onMovieClick: (Int) -> Unit,
    onCategoryChange: (MovieCategory) -> Unit,
    onRetryFeatured: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryList: () -> Unit,
) {
    val listState = rememberLazyListState()

    // Infinite scroll: muat halaman berikutnya saat sisa item tinggal sedikit
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 4
        }
    }
    LaunchedEffect(nearEnd) { if (nearEnd) onLoadMore() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Movies") }) },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ---- Bagian atas: carousel horizontal (Popular / Now Playing) ----
            item(key = "featured-header") {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MovieCategory.entries.forEach { category ->
                        FilterChip(
                            selected = state.category == category,
                            onClick = { onCategoryChange(category) },
                            label = { Text(category.label) },
                        )
                    }
                }
            }
            item(key = "featured-row") {
                when (val featured = state.featured) {
                    FeaturedState.Loading -> LoadingBox()
                    is FeaturedState.Error -> ErrorBox(featured.message, onRetryFeatured)
                    is FeaturedState.Success -> LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(featured.movies, key = { it.id }) { movie ->
                            MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
                        }
                    }
                }
            }

            // ---- Bagian bawah: seluruh movie (scrollable, paging) ----
            item(key = "all-title") {
                Text(
                    text = "All Movies",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            items(state.movies, key = { "movie-${it.id}" }) { movie ->
                MovieListItem(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item(key = "list-footer") {
                when {
                    state.listError != null -> ErrorBox(state.listError, onRetryList)
                    state.isLoadingList -> LoadingBox()
                    state.endReached -> Text(
                        text = "No more movies",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}