package com.nkds.hosikoouma.nouma.features.media

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.nkds.hosikoouma.nouma.data.local.MessageType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewerScreen(
    viewModel: MediaViewerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { uiState.mediaItems.size })

    LaunchedEffect(uiState.initialPage, uiState.mediaItems.isNotEmpty()) {
        if (uiState.mediaItems.isNotEmpty() && pagerState.currentPage != uiState.initialPage) {
            pagerState.scrollToPage(uiState.initialPage)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) { pageIndex ->
            if (uiState.mediaItems.isEmpty()) return@HorizontalPager
            val mediaItem = uiState.mediaItems[pageIndex]

            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                when (mediaItem.type) {
                    MessageType.IMAGE -> {
                        AsyncImage(
                            model = mediaItem.text?.let { Uri.parse(it) },
                            contentDescription = "Full screen image",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    MessageType.VIDEO -> {
                        val context = LocalContext.current
                        val exoPlayer = remember {
                            ExoPlayer.Builder(context).build().apply {
                                mediaItem.text?.let { uriString ->
                                    setMediaItem(MediaItem.fromUri(Uri.parse(uriString)))
                                    prepare()
                                }
                            }
                        }

                        val isVisible = pagerState.settledPage == pageIndex
                        
                        LaunchedEffect(isVisible) {
                            if (isVisible) {
                                exoPlayer.play()
                            } else {
                                exoPlayer.pause()
                            }
                        }

                        DisposableEffect(Unit) {
                            onDispose { exoPlayer.release() }
                        }

                        AndroidView(
                            factory = { PlayerView(it).apply { player = exoPlayer } },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
