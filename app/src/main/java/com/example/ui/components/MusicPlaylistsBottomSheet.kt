package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MusicLibrary
import com.example.data.MusicTrack
import com.example.data.SpotifyConnectionState
import com.example.data.SpotifyFitnessPlaylist
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlaylistsBottomSheet(
    currentTrackId: String,
    spotifyState: SpotifyConnectionState,
    isPlaying: Boolean,
    onConnectSpotify: () -> Unit,
    onDisconnectSpotify: () -> Unit,
    onSelectPlaylist: (SpotifyFitnessPlaylist) -> Unit,
    onOpenPlaylistInSpotify: (SpotifyFitnessPlaylist) -> Unit,
    onSelectTrack: (MusicTrack) -> Unit,
    onSelectLocalAudioUri: (Uri, String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPlaylistId by remember(spotifyState.activePlaylistId) {
        mutableStateOf(spotifyState.activePlaylistId)
    }

    val selectedPlaylist = MusicLibrary.getPlaylistById(selectedPlaylistId)

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Mi Canción Gym"
            onSelectLocalAudioUri(uri, fileName)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Reproductor Spotify & Música In-App",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Escucha tus playlists y tu música dentro de la misma app sin salir",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Embedded In-App Spotify Player (Never opens external app!)
            InAppSpotifyEmbedPlayer(
                spotifyPlaylistId = selectedPlaylist.spotifyPlaylistId,
                heightDp = 152
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Connect Local Device Audio / In-App Music Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = Color(0xFF0A1202)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("btn_pick_device_audio")
                ) {
                    Icon(Icons.Filled.AudioFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cargar Música de mi Celular", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }

                if (spotifyState.isConnected) {
                    OutlinedButton(
                        onClick = onDisconnectSpotify,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCoral),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("btn_disconnect_spotify")
                    ) {
                        Icon(Icons.Filled.LinkOff, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pausar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onConnectSpotify,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpotifyGreen,
                            contentColor = Color(0xFF0A1202)
                        ),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("btn_connect_spotify_sheet")
                    ) {
                        Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activar In-App", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Curated Fitness Playlists Carousel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playlists de Entrenamiento (Reproducción Interna)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${MusicLibrary.curatedPlaylists.size} mixes",
                    fontSize = 11.sp,
                    color = SpotifyGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MusicLibrary.curatedPlaylists, key = { it.id }) { playlist ->
                    val isPlaylistSelected = playlist.id == selectedPlaylistId
                    CuratedPlaylistCard(
                        playlist = playlist,
                        isSelected = isPlaylistSelected,
                        onSelectAndPlay = {
                            selectedPlaylistId = playlist.id
                            onSelectPlaylist(playlist)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Tracks inside Selected Curated Playlist
            Text(
                text = "Pistas en la App: ${selectedPlaylist.name} (${selectedPlaylist.bpmRange})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                items(selectedPlaylist.tracks, key = { it.id }) { track ->
                    val isSelected = track.id == currentTrackId

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SpotifyGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = if (isSelected) BorderStroke(1.dp, SpotifyGreen) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectTrack(track)
                            }
                            .testTag("track_item_${track.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(11.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (isSelected) SpotifyGreen else MaterialTheme.colorScheme.surface,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isSelected && isPlaying) Icons.Filled.GraphicEq else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF0F141C) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) SpotifyGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${track.artist} • Sonando en la app",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryOrange.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${track.bpm} BPM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CuratedPlaylistCard(
    playlist: SpotifyFitnessPlaylist,
    isSelected: Boolean,
    onSelectAndPlay: () -> Unit
) {
    val accentColor = Color(playlist.accentHex)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .width(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelectAndPlay() }
            .testTag("spotify_playlist_${playlist.id}")
    ) {
        Column(modifier = Modifier.padding(11.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = playlist.workoutCategory,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = playlist.bpmRange,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = playlist.name,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onSelectAndPlay,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) SpotifyGreen else MaterialTheme.colorScheme.primary,
                    contentColor = Color(0xFF0A1202)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSelected) "Sonando en la App" else "Escuchar Aquí",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
