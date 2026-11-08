package com.example.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.MusicLibrary
import com.example.data.MusicTrack
import com.example.data.SpotifyConnectionState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange

val SpotifyGreen = Color(0xFF1DB954)
val YouTubeRed = Color(0xFFFF0033)

@Composable
fun WorkoutMiniMusicPlayer(
    currentTrack: MusicTrack,
    spotifyState: SpotifyConnectionState,
    isPlaying: Boolean,
    progressPercent: Float,
    onTogglePlay: () -> Unit,
    onPreviousTrack: () -> Unit = {},
    onNextTrack: () -> Unit,
    onConnectSpotify: () -> Unit = {},
    onSelectLocalAudioUri: (Uri, String) -> Unit = { _, _ -> },
    onOpenPlaylistSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activePlaylist = MusicLibrary.getPlaylistById(currentTrack.playlistId)
    var showEmbeddedSpotifyPlayer by remember { mutableStateOf(false) }

    // Picker for user's own local MP3/Audio files so they can connect their music right inside the app
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Mi Canción Gym"
            onSelectLocalAudioUri(uri, fileName)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "equalizer_bars")
    val barHeight1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val barHeight2 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val barHeight3 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
        label = "b3"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        border = BorderStroke(
            1.dp,
            if (showEmbeddedSpotifyPlayer || isPlaying) SpotifyGreen.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("workout_music_player")
    ) {
        Column {
            // Track progress bar on top edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent.coerceIn(0f, 1f))
                        .height(3.dp)
                        .background(if (showEmbeddedSpotifyPlayer) SpotifyGreen else NeonLime)
                )
            }

            // Top Row: In-App Spotify Player toggle + Connect Device Music
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenPlaylistSheet() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isPlaying || showEmbeddedSpotifyPlayer) SpotifyGreen else NeonLime,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "En la App: ${activePlaylist.name}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Connect local MP3 / Audio file inside the app
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                audioPickerLauncher.launch("audio/*")
                            }
                            .testTag("btn_connect_device_music")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AudioFile,
                                contentDescription = "Conectar mi música",
                                tint = AccentCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Mi MP3",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                    }

                    // Toggle Embedded In-App Spotify Player (Never leaves the app!)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showEmbeddedSpotifyPlayer) SpotifyGreen else SpotifyGreen.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, SpotifyGreen),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showEmbeddedSpotifyPlayer = !showEmbeddedSpotifyPlayer
                                onConnectSpotify()
                            }
                            .testTag("btn_spotify_mini_connect")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showEmbeddedSpotifyPlayer) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Spotify integrado en la app",
                                tint = if (showEmbeddedSpotifyPlayer) Color(0xFF0A1202) else SpotifyGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (showEmbeddedSpotifyPlayer) "Spotify In-App ▲" else "Spotify In-App ▼",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showEmbeddedSpotifyPlayer) Color(0xFF0A1202) else SpotifyGreen
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equalizer / Music Disc icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF0F141C), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenPlaylistSheet() }
                ) {
                    if (isPlaying) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val w = 3.dp.toPx()
                            val spacing = 3.dp.toPx()
                            val totalW = (w * 3) + (spacing * 2)
                            val startX = (size.width - totalW) / 2f

                            val h1 = barHeight1.dp.toPx().coerceAtMost(size.height)
                            val h2 = barHeight2.dp.toPx().coerceAtMost(size.height)
                            val h3 = barHeight3.dp.toPx().coerceAtMost(size.height)

                            drawRect(SpotifyGreen, Offset(startX, size.height - h1), Size(w, h1))
                            drawRect(NeonLime, Offset(startX + w + spacing, size.height - h2), Size(w, h2))
                            drawRect(AccentCyan, Offset(startX + (w + spacing) * 2, size.height - h3), Size(w, h3))
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = "Música",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Track title & BPM
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenPlaylistSheet() }
                ) {
                    Text(
                        text = currentTrack.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PrimaryOrange.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${currentTrack.bpm} BPM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryOrange,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentTrack.artist} • Sin salir de la app",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Controls: Playlist drawer, Previous, Play/Pause, Next
                IconButton(
                    onClick = onOpenPlaylistSheet,
                    modifier = Modifier.size(32.dp).testTag("btn_open_music_playlist")
                ) {
                    Icon(
                        imageVector = Icons.Filled.QueueMusic,
                        contentDescription = "Playlists en la App",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onPreviousTrack,
                    modifier = Modifier.size(32.dp).testTag("btn_music_prev")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Canción anterior",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(38.dp)
                        .background(SpotifyGreen, CircleShape)
                        .testTag("btn_music_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar música" else "Reproducir música",
                        tint = Color(0xFF0F141C),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onNextTrack,
                    modifier = Modifier.size(32.dp).testTag("btn_music_next")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Siguiente canción",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Embedded In-App Spotify Player (Plays directly inside the app without opening any external app!)
            AnimatedVisibility(visible = showEmbeddedSpotifyPlayer) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                ) {
                    InAppSpotifyEmbedPlayer(
                        spotifyPlaylistId = activePlaylist.spotifyPlaylistId,
                        heightDp = 152
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppSpotifyEmbedPlayer(
    spotifyPlaylistId: String,
    heightDp: Int = 152,
    modifier: Modifier = Modifier
) {
    val embedUrl = "https://open.spotify.com/embed/playlist/$spotifyPlaylistId?utm_source=generator&theme=0"
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF121212),
        border = BorderStroke(1.dp, SpotifyGreen.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(14.dp))
            .testTag("embedded_spotify_webview")
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString().orEmpty()
                            // Block any attempt to jump to external spotify:// or intent:// outside the app
                            return if (url.startsWith("http://") || url.startsWith("https://")) {
                                false
                            } else {
                                true
                            }
                        }
                    }
                    setBackgroundColor(android.graphics.Color.parseColor("#121212"))
                    loadUrl(embedUrl)
                }
            },
            update = { webView ->
                if (webView.url != embedUrl) {
                    webView.loadUrl(embedUrl)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
