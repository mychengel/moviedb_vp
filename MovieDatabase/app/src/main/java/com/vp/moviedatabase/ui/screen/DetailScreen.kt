package com.vp.moviedatabase.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vp.moviedatabase.model.MovieDetail
import com.vp.moviedatabase.ui.widgets.ErrorBox
import com.vp.moviedatabase.ui.widgets.LoadingBox
import com.vp.moviedatabase.ui.widgets.RatingBadge
import com.vp.moviedatabase.ui.widgets.RemoteImage
import com.vp.moviedatabase.viewmodel.DetailUiState
import com.vp.moviedatabase.viewmodel.DetailViewModel

@Composable
fun DetailScreen(
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailContent(state = state, onBackClick = onBackClick, onRetry = viewModel::load)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    state: DetailUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (state) {
                DetailUiState.Loading -> LoadingBox(Modifier.align(Alignment.Center))
                is DetailUiState.Error -> ErrorBox(state.message, onRetry, Modifier.align(Alignment.Center))
                is DetailUiState.Success -> MovieDetailBody(state.movie)
            }
        }
    }
}

@Composable
private fun MovieDetailBody(movie: MovieDetail) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Backdrop + poster yang menimpa sebagian backdrop
        Box {
            RemoteImage(
                url = movie.backdropUrl ?: movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            )
            RemoteImage(
                url = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .width(110.dp)
                    .aspectRatio(2f / 3f)
                    .align(Alignment.BottomStart)
                    .offset(y = 40.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }

        Column(
            modifier = Modifier.padding(start = 140.dp, end = 16.dp, top = 8.dp),
        ) {
            Text(movie.title, style = MaterialTheme.typography.titleLarge)
            if (movie.tagline.isNotEmpty()) {
                Text(
                    movie.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                RatingBadge(movie.rating)
                Text("${movie.voteCount} votes", style = MaterialTheme.typography.bodySmall)
                if (movie.releaseDate.isNotEmpty()) Text(movie.releaseDate, style = MaterialTheme.typography.bodySmall)
                movie.runtimeMinutes?.let { Text("${it / 60}h ${it % 60}m", style = MaterialTheme.typography.bodySmall) }
            }

            if (movie.genres.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(movie.genres) { genre ->
                        AssistChip(onClick = {}, label = { Text(genre) })
                    }
                }
            }

            Text("Overview", style = MaterialTheme.typography.titleMedium)
            Text(
                text = movie.overview.ifEmpty { "No overview available." },
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}