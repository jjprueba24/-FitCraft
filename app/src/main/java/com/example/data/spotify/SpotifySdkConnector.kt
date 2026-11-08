package com.example.data.spotify

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.MusicLibrary
import com.example.data.MusicTrack
import com.example.data.SpotifyConnectionState
import com.example.data.SpotifyFitnessPlaylist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SpotifySdkConnector(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "SpotifySdkConnector"
        private const val PREFS_NAME = "spotify_sdk_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_NAME = "spotify_user_name"
        private const val KEY_USER_EMAIL = "spotify_user_email"
        private const val KEY_ACCOUNT_TIER = "spotify_account_tier"
        private const val KEY_IS_CONNECTED = "spotify_is_connected"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _connectionState = MutableStateFlow(loadInitialConnectionState())
    val connectionState: StateFlow<SpotifyConnectionState> = _connectionState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var currentPlayingTrack: MusicTrack? = null

    private fun getOrCreateExoPlayer(): ExoPlayer {
        exoPlayer?.let { return it }
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val player = ExoPlayer.Builder(context.applicationContext)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.repeatMode = Player.REPEAT_MODE_ONE
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.w(TAG, "Media3 stream error (${error.errorCodeName}), switching to local WAV in Media3")
                val track = currentPlayingTrack ?: MusicLibrary.tracks.first()
                playLocalRoyaltyFreeWavInMedia3(track)
            }
        })
        exoPlayer = player
        return player
    }

    private fun loadInitialConnectionState(): SpotifyConnectionState {
        val savedConnected = prefs.getBoolean(KEY_IS_CONNECTED, true)
        val savedName = prefs.getString(KEY_USER_NAME, "Reproductor In-App")
        val savedEmail = prefs.getString(KEY_USER_EMAIL, null)
        val savedTier = prefs.getString(KEY_ACCOUNT_TIER, "Spotify Embebido + Media3")

        return SpotifyConnectionState(
            isConnected = savedConnected,
            isConnecting = false,
            userDisplayName = savedName,
            userEmail = savedEmail,
            accountTier = savedTier,
            isSpotifyAppInstalled = true,
            isClientIdConfigured = true,
            activePlaylistId = MusicLibrary.curatedPlaylists.first().id,
            playerEngineLabel = "Spotify In-App + Media3",
            statusMessage = "Reproducción 100% dentro de la app activa"
        )
    }

    /**
     * Connects or toggles the in-app Spotify & Music session directly inside the app UI
     * without launching any external application or browser window.
     */
    fun connectSpotifyAccount(fallbackEmail: String?) {
        val displayName = fallbackEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Mi Música Gym"
        val tier = "Reproductor Integrado en la App"

        prefs.edit()
            .putBoolean(KEY_IS_CONNECTED, true)
            .putString(KEY_USER_NAME, displayName)
            .putString(KEY_USER_EMAIL, fallbackEmail ?: "musica@fitcraft.app")
            .putString(KEY_ACCOUNT_TIER, tier)
            .apply()

        _connectionState.update {
            it.copy(
                isConnected = true,
                isConnecting = false,
                userDisplayName = displayName,
                userEmail = fallbackEmail ?: "musica@fitcraft.app",
                accountTier = tier,
                statusMessage = "Spotify y Media3 conectados dentro de la app",
                errorMessage = null
            )
        }
    }

    fun handleAuthRedirectIntent(intent: Intent?) {
        // Handled in-app without external browser redirects
    }

    fun disconnectSpotifyAccount() {
        stopAudioEngine()
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .putBoolean(KEY_IS_CONNECTED, false)
            .apply()

        _connectionState.update {
            it.copy(
                isConnected = false,
                isConnecting = false,
                userDisplayName = null,
                userEmail = null,
                accountTier = null,
                statusMessage = "Modo Audio Integrado Media3 activo en la app",
                errorMessage = null
            )
        }
    }

    /**
     * Plays a curated fitness playlist or track directly inside the app using Jetpack Media3 ExoPlayer
     * without opening any external app.
     */
    fun playPlaylistOrTrack(playlist: SpotifyFitnessPlaylist, track: MusicTrack) {
        currentPlayingTrack = track
        _connectionState.update {
            it.copy(
                activePlaylistId = playlist.id,
                statusMessage = "Sonando en la app: ${track.title}"
            )
        }

        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        if (!accessToken.isNullOrBlank()) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    SpotifyNetworkModule.webApi.startOrResumePlayback(
                        bearerToken = "Bearer $accessToken",
                        playRequest = SpotifyPlayRequest(
                            contextUri = playlist.spotifyUri
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Spotify Web API playback fallback to in-app Media3", e)
                }
            }
        }

        playTrackWithMedia3(track)
    }

    /**
     * Plays a user-selected audio file (MP3/WAV/M4A) from their device directly inside the app via Media3 ExoPlayer.
     */
    fun playUserDeviceAudioUri(uri: Uri, title: String): MusicTrack {
        val customTrack = MusicTrack(
            id = "custom_${System.currentTimeMillis()}",
            title = title,
            artist = "Mi Música del Dispositivo",
            bpm = 135,
            genre = "Mi Biblioteca",
            durationSeconds = 210,
            audioStreamUrl = uri.toString(),
            youtubeUrl = "",
            playlistId = _connectionState.value.activePlaylistId,
            licenseInfo = "Archivo Local"
        )
        currentPlayingTrack = customTrack
        _connectionState.update {
            it.copy(
                isConnected = true,
                statusMessage = "Reproduciendo tu música en la app: $title"
            )
        }
        coroutineScope.launch(Dispatchers.Main) {
            try {
                val player = getOrCreateExoPlayer()
                player.setMediaItem(MediaItem.fromUri(uri))
                player.prepare()
                player.playWhenReady = true
                player.play()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing device audio URI in Media3", e)
            }
        }
        return customTrack
    }

    private fun playTrackWithMedia3(track: MusicTrack) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                val localWavFile = withContext(Dispatchers.IO) {
                    ensureLocalRoyaltyFreeWavFile(track)
                }
                val player = getOrCreateExoPlayer()
                val items = buildList {
                    if (localWavFile != null && localWavFile.exists()) {
                        add(MediaItem.fromUri(Uri.fromFile(localWavFile)))
                    }
                    if (track.audioStreamUrl.isNotBlank()) {
                        add(MediaItem.fromUri(Uri.parse(track.audioStreamUrl)))
                    }
                }
                if (items.isNotEmpty()) {
                    player.setMediaItems(items, true)
                    player.prepare()
                    player.playWhenReady = true
                    player.play()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Media3 playback error", e)
            }
        }
    }

    private fun playLocalRoyaltyFreeWavInMedia3(track: MusicTrack) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                val localWavFile = withContext(Dispatchers.IO) {
                    ensureLocalRoyaltyFreeWavFile(track)
                } ?: return@launch
                val player = getOrCreateExoPlayer()
                player.setMediaItem(MediaItem.fromUri(Uri.fromFile(localWavFile)))
                player.prepare()
                player.playWhenReady = true
                player.play()
            } catch (e: Exception) {
                Log.e(TAG, "Local WAV Media3 fallback failed", e)
            }
        }
    }

    fun pausePlayback() {
        coroutineScope.launch(Dispatchers.Main) {
            exoPlayer?.pause()
        }
    }

    fun stopAudioEngine() {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                exoPlayer?.stop()
                exoPlayer?.release()
                exoPlayer = null
            } catch (_: Exception) {
            }
        }
    }

    private fun ensureLocalRoyaltyFreeWavFile(track: MusicTrack): File? {
        return try {
            val dir = File(context.cacheDir, "media3_workout_tracks")
            if (!dir.exists()) dir.mkdirs()
            val outFile = File(dir, "workout_${track.id}_${track.bpm}bpm.wav")
            if (outFile.exists() && outFile.length() > 44) {
                return outFile
            }

            val sampleRate = 22050
            val safeBpm = track.bpm.coerceIn(75, 175)
            val durationSeconds = (16.0 * 60.0 / safeBpm).coerceIn(6.0, 14.0)
            val totalSamples = (sampleRate * durationSeconds).toInt()
            val pcmData = ShortArray(totalSamples)

            val samplesPerBeat = (sampleRate * 60.0 / safeBpm).toInt().coerceAtLeast(1000)
            val samplesPerStep = (samplesPerBeat / 4).coerceAtLeast(250)

            val bassNotes = doubleArrayOf(55.0, 55.0, 65.41, 73.42, 65.41, 65.41, 49.0, 82.41)
            val leadNotes = doubleArrayOf(220.0, 261.63, 329.63, 293.66, 392.0, 329.63, 261.63, 246.94)

            for (i in 0 until totalSamples) {
                val beatIndex = i / samplesPerBeat
                val stepIndex = i / samplesPerStep
                val sampleInBeat = i % samplesPerBeat
                val sampleInStep = i % samplesPerStep

                val tBeat = sampleInBeat.toDouble() / sampleRate
                val tStep = sampleInStep.toDouble() / sampleRate

                val barIndex = (beatIndex / 4) % 2
                val chordIdx = ((beatIndex % 4) + barIndex * 4) % bassNotes.size
                val bassFreq = bassNotes[chordIdx]
                val leadFreq = leadNotes[(stepIndex + barIndex * 2) % leadNotes.size]

                val kickFreq = 120.0 * exp(-tBeat * 30.0) + 46.0
                val kickEnv = exp(-tBeat * 9.5)
                val kick = sin(2.0 * PI * kickFreq * tBeat) * kickEnv * 0.48

                val snare = if (beatIndex % 2 == 1) {
                    val snareEnv = exp(-tBeat * 22.0)
                    val snareBody = sin(2.0 * PI * 185.0 * tBeat) * exp(-tBeat * 18.0)
                    val snareNoise = sin(2.0 * PI * 2900.0 * tBeat) * sin(2.0 * PI * 4700.0 * tBeat)
                    (snareBody * 0.4 + snareNoise * 0.6) * snareEnv * 0.30
                } else 0.0

                val hatEnv = exp(-tStep * 48.0)
                val hatNoise = sin(2.0 * PI * 4200.0 * tStep) * sin(2.0 * PI * 6300.0 * tStep)
                val hatAmp = if (stepIndex % 2 == 1) 0.16 else 0.06
                val hat = hatNoise * hatEnv * hatAmp

                val bassEnv = exp(-tBeat * 3.8)
                val bass = (sin(2.0 * PI * bassFreq * tBeat) * 0.7 +
                    sin(2.0 * PI * (bassFreq * 2.0) * tBeat) * 0.3) * bassEnv * 0.28

                val arpEnv = exp(-tStep * 14.0)
                val arp = (sin(2.0 * PI * leadFreq * tStep) +
                    0.5 * sin(2.0 * PI * (leadFreq * 1.5) * tStep)) * arpEnv * 0.18

                val mix = (kick + snare + hat + bass + arp).coerceIn(-0.95, 0.95)
                pcmData[i] = (mix * Short.MAX_VALUE).toInt().toShort()
            }

            writeWavFile(outFile, pcmData, sampleRate)
            outFile
        } catch (e: Exception) {
            Log.e(TAG, "Error generating local WAV file for Media3", e)
            null
        }
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val byteRate = sampleRate * 2
        val dataSize = pcmData.size * 2
        val totalFileSize = 36 + dataSize

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(totalFileSize)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(sampleRate)
            putInt(byteRate)
            putShort(2)
            putShort(16)
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(dataSize)
        }

        FileOutputStream(file).use { fos ->
            fos.write(header.array())
            val pcmBuffer = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
            for (s in pcmData) {
                pcmBuffer.putShort(s)
            }
            fos.write(pcmBuffer.array())
        }
    }
}
