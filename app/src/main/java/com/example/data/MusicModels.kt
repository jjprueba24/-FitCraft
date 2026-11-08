package com.example.data

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val bpm: Int,
    val genre: String, // "Fuerza / Hardstyle", "Cardio / High Energy", "Warmup / Synthwave", "Recovery / Lo-Fi"
    val durationSeconds: Int,
    val isCopyrightFree: Boolean = true
)

object MusicLibrary {
    val tracks = listOf(
        MusicTrack("m1", "Phonk Overload • Heavy Lifting", "FitFlow Beats", 145, "Fuerza & Hipertrofia", 210),
        MusicTrack("m2", "Adrenaline Rush • Bass Workout", "Gym Core Sound", 150, "Fuerza & Hipertrofia", 195),
        MusicTrack("m3", "Neon Speed • HIIT Cardio", "CyberPulse", 138, "Cardio & Quema", 225),
        MusicTrack("m4", "Endurance Pulse • Treadmill Run", "Beat Runner", 140, "Cardio & Quema", 240),
        MusicTrack("m5", "Deep Focus • Lo-Fi Stretching", "Chill Mind", 85, "Estiramiento & Relax", 180),
        MusicTrack("m6", "Pre-Workout Ignition • Bass Drop", "DropForge", 155, "Fuerza & Hipertrofia", 200)
    )
}
