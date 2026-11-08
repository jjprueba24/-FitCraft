package com.example.data

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val bpm: Int,
    val genre: String,
    val durationSeconds: Int,
    val audioStreamUrl: String,
    val youtubeUrl: String,
    val spotifyTrackUri: String = "spotify:track:4cOdK2wGLETKBW3PvgPWqT",
    val playlistId: String = "pl_beast_mode",
    val isCopyrightFree: Boolean = true,
    val licenseInfo: String = "CC-BY / Royalty-Free"
)

data class SpotifyFitnessPlaylist(
    val id: String,
    val name: String,
    val subtitle: String,
    val workoutCategory: String, // "Fuerza", "Hipertrofia", "Cardio HIIT", "Motivación Latina", "Movilidad"
    val spotifyPlaylistId: String,
    val spotifyUri: String, // e.g. "spotify:playlist:37i9dQZF1DX76Wlfdnj7AP"
    val youtubeUrl: String, // Direct YouTube workout mix search / playlist URL
    val bpmRange: String,
    val accentHex: Long,
    val tracks: List<MusicTrack>
)

data class SpotifyConnectionState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val userDisplayName: String? = null,
    val userEmail: String? = null,
    val accountTier: String? = null, // "Spotify Premium", "Spotify Free", "Spotify App Remote"
    val isSpotifyAppInstalled: Boolean = false,
    val isClientIdConfigured: Boolean = false,
    val activePlaylistId: String = "pl_beast_mode",
    val playerEngineLabel: String = "Media3 ExoPlayer (Licencia Libre)",
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

object MusicLibrary {

    val curatedPlaylists: List<SpotifyFitnessPlaylist> = listOf(
        SpotifyFitnessPlaylist(
            id = "pl_beast_mode",
            name = "Beast Mode • Heavy Lifting",
            subtitle = "Energía explosiva para series pesadas y récords personales (PR)",
            workoutCategory = "Fuerza & Hipertrofia",
            spotifyPlaylistId = "37i9dQZF1DX76Wlfdnj7AP",
            spotifyUri = "spotify:playlist:37i9dQZF1DX76Wlfdnj7AP",
            youtubeUrl = "https://www.youtube.com/results?search_query=Beast+Mode+Heavy+Lifting+Gym+Workout+Music+Mix",
            bpmRange = "145 - 155 BPM",
            accentHex = 0xFF1DB954,
            tracks = listOf(
                MusicTrack(
                    id = "m1",
                    title = "Phonk Overload • Heavy Lifting",
                    artist = "SoundHelix / FitFlow CC-BY",
                    bpm = 148,
                    genre = "Fuerza & Hipertrofia",
                    durationSeconds = 210,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=Aggressive+Gym+Phonk+Workout+Mix",
                    spotifyTrackUri = "spotify:track:0VjIjW4GlUZAMYd2vXMi3b",
                    playlistId = "pl_beast_mode"
                ),
                MusicTrack(
                    id = "m2",
                    title = "Adrenaline Rush • Barbell Squat",
                    artist = "Gym Core CC-BY",
                    bpm = 152,
                    genre = "Fuerza & Hipertrofia",
                    durationSeconds = 195,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=Heavy+Squat+Gym+Motivation+Music",
                    spotifyTrackUri = "spotify:track:7KXjTSCq5nL1LoYtL7XAwS",
                    playlistId = "pl_beast_mode"
                ),
                MusicTrack(
                    id = "m6",
                    title = "Pre-Workout Ignition • Bass Drop",
                    artist = "DropForge CC0",
                    bpm = 155,
                    genre = "Fuerza & Hipertrofia",
                    durationSeconds = 200,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=Pre+Workout+Bass+Drop+Gym+Music",
                    spotifyTrackUri = "spotify:track:2XU0oxnq2qxCpomAAuJY8K",
                    playlistId = "pl_beast_mode"
                )
            )
        ),
        SpotifyFitnessPlaylist(
            id = "pl_power_workout",
            name = "Power Workout • Push / Pull",
            subtitle = "Ritmo constante para hipertrofia y bombeo muscular",
            workoutCategory = "Hipertrofia",
            spotifyPlaylistId = "37i9dQZF1DX70RN3TfWWJh",
            spotifyUri = "spotify:playlist:37i9dQZF1DX70RN3TfWWJh",
            youtubeUrl = "https://www.youtube.com/results?search_query=Push+Pull+Legs+Bodybuilding+Workout+Music",
            bpmRange = "128 - 136 BPM",
            accentHex = 0xFFCCFF00,
            tracks = listOf(
                MusicTrack(
                    id = "m7",
                    title = "Iron Pump • Chest & Back",
                    artist = "Titan Audio CC-BY",
                    bpm = 132,
                    genre = "Hipertrofia",
                    durationSeconds = 215,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=Chest+and+Back+Gym+Pump+Music",
                    spotifyTrackUri = "spotify:track:3n3Ppam7vgaVa1iaRUc9Lp",
                    playlistId = "pl_power_workout"
                ),
                MusicTrack(
                    id = "m8",
                    title = "Progressive Overload • Synthwave",
                    artist = "Apex Rep CC-BY",
                    bpm = 135,
                    genre = "Hipertrofia",
                    durationSeconds = 204,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=Synthwave+Gym+Workout+Music+Mix",
                    spotifyTrackUri = "spotify:track:6habFhsOp2NvshLv26DqMb",
                    playlistId = "pl_power_workout"
                )
            )
        ),
        SpotifyFitnessPlaylist(
            id = "pl_cardio_hiit",
            name = "Cardio HIIT • Quema Grasa",
            subtitle = "Cadencia rápida para sprints, burpees y déficit calórico",
            workoutCategory = "Cardio & Quema",
            spotifyPlaylistId = "37i9dQZF1DX5gQonLbZD9s",
            spotifyUri = "spotify:playlist:37i9dQZF1DX5gQonLbZD9s",
            youtubeUrl = "https://www.youtube.com/results?search_query=HIIT+Cardio+Workout+Music+140+BPM",
            bpmRange = "138 - 144 BPM",
            accentHex = 0xFF00E5FF,
            tracks = listOf(
                MusicTrack(
                    id = "m3",
                    title = "Neon Speed • HIIT Cardio",
                    artist = "CyberPulse CC-BY",
                    bpm = 138,
                    genre = "Cardio & Quema",
                    durationSeconds = 225,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=High+Energy+Cardio+HIIT+Music",
                    spotifyTrackUri = "spotify:track:1r9xUipOqoNwggBpENDsvJ",
                    playlistId = "pl_cardio_hiit"
                ),
                MusicTrack(
                    id = "m4",
                    title = "Endurance Pulse • Treadmill Run",
                    artist = "Beat Runner CC-BY",
                    bpm = 142,
                    genre = "Cardio & Quema",
                    durationSeconds = 240,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=Running+Treadmill+140+BPM+Workout+Music",
                    spotifyTrackUri = "spotify:track:5HCyWlXZPP0y6Gqq8TgA20",
                    playlistId = "pl_cardio_hiit"
                )
            )
        ),
        SpotifyFitnessPlaylist(
            id = "pl_latin_gym",
            name = "Motivación Gym • Urbano & Electro",
            subtitle = "Energía latina de alto octanaje para no fallar ni una repetición",
            workoutCategory = "Energía & Ritmo",
            spotifyPlaylistId = "37i9dQZF1DX35X4JNyBWtb",
            spotifyUri = "spotify:playlist:37i9dQZF1DX35X4JNyBWtb",
            youtubeUrl = "https://www.youtube.com/results?search_query=Musica+Para+Entrenar+Gym+Latino+Electro+Mix",
            bpmRange = "125 - 134 BPM",
            accentHex = 0xFFFF6D00,
            tracks = listOf(
                MusicTrack(
                    id = "m9",
                    title = "Fuego en el Rack • Electro Urbano",
                    artist = "Calistenia & Barra CC-BY",
                    bpm = 128,
                    genre = "Energía & Ritmo",
                    durationSeconds = 198,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=Electro+Latino+Gym+Workout+Mix",
                    spotifyTrackUri = "spotify:track:4Dvkj6JhhA12EX05fT7y2e",
                    playlistId = "pl_latin_gym"
                ),
                MusicTrack(
                    id = "m10",
                    title = "Sin Excusas • Última Serie",
                    artist = "Voltaje Gym CC-BY",
                    bpm = 132,
                    genre = "Energía & Ritmo",
                    durationSeconds = 190,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=Motivacion+Gym+Pesas+Musica+Urbana",
                    spotifyTrackUri = "spotify:track:2tTmW7RDtMQtBk7m2rYeSw",
                    playlistId = "pl_latin_gym"
                )
            )
        ),
        SpotifyFitnessPlaylist(
            id = "pl_cooldown",
            name = "Mobility & Cool Down • Lo-Fi",
            subtitle = "Frecuencias relajantes para estiramiento post-entreno y recuperación",
            workoutCategory = "Estiramiento & Relax",
            spotifyPlaylistId = "37i9dQZF1DWZeKCadgRdKQ",
            spotifyUri = "spotify:playlist:37i9dQZF1DWZeKCadgRdKQ",
            youtubeUrl = "https://www.youtube.com/results?search_query=Post+Workout+Stretching+Cool+Down+LoFi+Music",
            bpmRange = "80 - 90 BPM",
            accentHex = 0xFFB388FF,
            tracks = listOf(
                MusicTrack(
                    id = "m5",
                    title = "Deep Focus • Lo-Fi Stretching",
                    artist = "Chill Mind CC-BY",
                    bpm = 85,
                    genre = "Estiramiento & Relax",
                    durationSeconds = 180,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=LoFi+Chill+Stretching+Mobility+Music",
                    spotifyTrackUri = "spotify:track:5W3cjX2J3tjhG8zb6u0qHn",
                    playlistId = "pl_cooldown"
                ),
                MusicTrack(
                    id = "m11",
                    title = "Recovery Breath • CNS Reset",
                    artist = "Zen Athlete CC-BY",
                    bpm = 82,
                    genre = "Estiramiento & Relax",
                    durationSeconds = 210,
                    audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                    youtubeUrl = "https://www.youtube.com/results?search_query=Post+Gym+Recovery+Ambient+Music",
                    spotifyTrackUri = "spotify:track:0VjIjW4GlUZAMYd2vXMi3b",
                    playlistId = "pl_cooldown"
                )
            )
        )
    )

    val tracks: List<MusicTrack> = curatedPlaylists.flatMap { it.tracks }

    fun getPlaylistById(playlistId: String): SpotifyFitnessPlaylist {
        return curatedPlaylists.find { it.id == playlistId } ?: curatedPlaylists.first()
    }
}
