package com.example.ui.components

import android.content.Context
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser
import com.example.data.Exercise
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.floor
import kotlin.math.min

data class GymManeArtData(
    val slug: String,
    val frames: List<android.graphics.Path>,
    val bounds: RectF
)

object GymManeArtCache {
    private val cache = LinkedHashMap<String, GymManeArtData>(36, 0.75f, true)

    suspend fun loadArt(context: Context, slug: String): GymManeArtData? {
        synchronized(cache) {
            cache[slug]?.let { return it }
        }
        return withContext(Dispatchers.IO) {
            try {
                val lines = context.assets.open("art/$slug.txt")
                    .bufferedReader()
                    .useLines { seq ->
                        seq.map { it.trim() }.filter { it.isNotEmpty() }.toList()
                    }
                if (lines.isEmpty()) return@withContext null

                val frames = mutableListOf<android.graphics.Path>()
                val unionBounds = RectF()
                val tmpBounds = RectF()

                for (d in lines) {
                    val path = PathParser.createPathFromPathData(d) ?: continue
                    path.fillType = android.graphics.Path.FillType.EVEN_ODD
                    path.computeBounds(tmpBounds, true)
                    if (frames.isEmpty()) {
                        unionBounds.set(tmpBounds)
                    } else {
                        unionBounds.union(tmpBounds)
                    }
                    frames.add(path)
                }

                if (frames.isEmpty() || unionBounds.isEmpty) return@withContext null
                val data = GymManeArtData(slug = slug, frames = frames, bounds = unionBounds)
                synchronized(cache) {
                    if (cache.size >= 32) {
                        val firstKey = cache.keys.firstOrNull()
                        if (firstKey != null) cache.remove(firstKey)
                    }
                    cache[slug] = data
                }
                data
            } catch (_: Exception) {
                null
            }
        }
    }
}

/**
 * Maps each exercise in FitCraft to its authentic multi-frame vector animation slug from InlitX/GymMane (`assets/art/<slug>.txt`).
 */
fun resolveGymManeArtSlug(exercise: Exercise): String {
    val text = "${exercise.name} ${exercise.muscleGroup}".lowercase()
    return when {
        text.contains("inclinado") && text.contains("mancuerna") -> "incline-dumbbell-press"
        text.contains("inclinado") -> "incline-bench-press"
        text.contains("banca") && text.contains("mancuerna") -> "dumbbell-bench-press"
        text.contains("banca") -> "bench-press"
        text.contains("cruces") || text.contains("polea en media") -> "cable-fly"
        text.contains("aperturas") -> "dumbbell-fly"
        text.contains("fondos") -> "chest-dip"
        text.contains("flexiones") -> "push-up"
        text.contains("dominada") -> "pull-up"
        text.contains("jalón") || text.contains("jalon") -> "lat-pulldown"
        text.contains("pendlay") || text.contains("remo con barra") -> "pendlay-row"
        text.contains("remo en polea") -> "seated-row"
        text.contains("remo") && text.contains("mancuerna") -> "one-arm-dumbbell-row"
        text.contains("remo") -> "barbell-row"
        text.contains("face pull") -> "face-pull"
        text.contains("militar") && text.contains("mancuerna") -> "seated-dumbbell-press"
        text.contains("militar") || text.contains("hombro") && text.contains("press") -> "overhead-press"
        text.contains("laterales") && text.contains("polea") -> "cable-lateral-raise"
        text.contains("laterales") -> "lateral-raise"
        text.contains("posterior") -> "rear-delt-fly"
        text.contains("goblet") -> "goblet-squat"
        text.contains("hack") -> "hack-squat"
        text.contains("búlgara") || text.contains("bulgara") -> "bulgarian-split-squat"
        text.contains("sentadilla") -> "squat"
        text.contains("prensa") -> "leg-press"
        text.contains("zancada") -> "walking-lunge"
        text.contains("extensión de cuádriceps") || text.contains("extension de cuadriceps") -> "leg-extension"
        text.contains("curl femoral") -> "lying-leg-curl"
        text.contains("rumano") && text.contains("mancuerna") -> "dumbbell-romanian-deadlift"
        text.contains("rumano") -> "romanian-deadlift"
        text.contains("peso muerto") -> "deadlift"
        text.contains("hip thrust") -> "hip-thrust"
        text.contains("puente") -> "glute-bridge"
        text.contains("talones") || text.contains("gemelo") -> "standing-calf-raise"
        text.contains("martillo") -> "hammer-curl"
        text.contains("barra z") -> "ez-bar-curl"
        text.contains("curl") && text.contains("inclinado") -> "incline-dumbbell-curl"
        text.contains("curl") -> "bicep-curl"
        text.contains("francés") || text.contains("frances") || text.contains("skull") -> "skull-crusher"
        text.contains("tríceps") || text.contains("triceps") -> "rope-tricep-pushdown"
        text.contains("crunch") && text.contains("polea") -> "cable-crunch"
        text.contains("crunch") -> "crunch"
        text.contains("plancha") -> "plank"
        text.contains("rueda abdominal") -> "ab-wheel"
        text.contains("cuerda") || text.contains("saltar") -> "jump-rope"
        text.contains("burpee") || text.contains("sprint") || text.contains("hiit") -> "burpee"
        text.contains("caminata") -> "treadmill-incline-walk"
        else -> "squat"
    }
}

/**
 * Full-width interactive animation stage rendering the authentic multi-frame vector animations
 * from InlitX/GymMane (`assets/art/<slug>.txt`) on EVERY exercise card.
 */
@Composable
fun ExerciseAnimationStage(
    exercise: Exercise,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val slug = remember(exercise.id, exercise.name, exercise.muscleGroup) {
        resolveGymManeArtSlug(exercise)
    }

    var artData by remember(slug) { mutableStateOf<GymManeArtData?>(null) }
    var isPaused by remember { mutableStateOf(false) }
    var isSlowMotion by remember { mutableStateOf(false) }
    var showPhotoOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(slug) {
        artData = GymManeArtCache.loadArt(context, slug)
    }

    val cycleDurationMs = if (isSlowMotion) 3120 else 1560
    val transition = rememberInfiniteTransition(label = "gymmane_art_${exercise.id}")
    val rawAnimT by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_t_${exercise.id}"
    )

    val animT = if (isPaused) 0.5f else rawAnimT
    val isConcentric = animT in 0.15f..0.65f

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0A0F18),
        border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.28f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Top bar: GymMane Live Animation Title + Slow-Mo (0.5x) / Pause / Photo Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111927))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isConcentric) NeonLime else AccentCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Animación Técnica GymMane • ${slug.replace('-', ' ').uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Slow motion toggle (0.5x / 1x)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSlowMotion) AccentCyan.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isSlowMotion = !isSlowMotion }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SlowMotionVideo,
                                contentDescription = "Cámara lenta",
                                tint = if (isSlowMotion) AccentCyan else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isSlowMotion) "0.5x" else "1x",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSlowMotion) AccentCyan else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Pause / Play toggle
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isPaused) PrimaryOrange.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isPaused = !isPaused }
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = "Pausar animación",
                            tint = if (isPaused) PrimaryOrange else NeonLime,
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                .size(13.dp)
                        )
                    }

                    // Optional Photo Guide Toggle if exercise also has a real photo
                    if (exercise.imageDrawableRes != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (showPhotoOverlay) NeonLime else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showPhotoOverlay = !showPhotoOverlay }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = "Alternar foto",
                                    tint = if (showPhotoOverlay) Color(0xFF0F141C) else NeonLime,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (showPhotoOverlay) "Animación" else "Foto",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (showPhotoOverlay) Color(0xFF0F141C) else NeonLime
                                )
                            }
                        }
                    }
                }
            }

            // Main Stage Area (195.dp height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(195.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF101827), Color(0xFF162236))
                        )
                    )
            ) {
                if (showPhotoOverlay && exercise.imageDrawableRes != null) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = exercise.imageDrawableRes),
                        contentDescription = "Guía visual de ${exercise.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val loaded = artData
                    val paint = remember {
                        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.FILL
                        }
                    }
                    val transformMatrix = remember { Matrix() }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Subtle anatomical studio grid
                        val w = size.width
                        val h = size.height
                        for (i in 1..3) {
                            val y = h * (i / 4f)
                            drawLine(
                                color = Color.White.copy(alpha = 0.04f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                        }

                        if (loaded != null && loaded.frames.isNotEmpty()) {
                            val b = loaded.bounds
                            val pad = min(w, h) * 0.07f
                            val scale = min(
                                (w - pad * 2f) / b.width().coerceAtLeast(1f),
                                (h - pad * 2f) / b.height().coerceAtLeast(1f)
                            )
                            if (scale > 0f) {
                                val n = loaded.frames.size
                                val nativeCanvas = drawContext.canvas.nativeCanvas
                                nativeCanvas.save()
                                nativeCanvas.translate(w / 2f, h / 2f)
                                nativeCanvas.scale(scale, scale)
                                nativeCanvas.translate(-b.centerX(), -b.centerY())

                                if (n < 2) {
                                    paint.color = android.graphics.Color.argb(245, 232, 240, 252)
                                    nativeCanvas.drawPath(loaded.frames.first(), paint)
                                } else {
                                    // Exact frame interpolation & cross-fade algorithm from GymMane _ArtPainter
                                    val steps = (n - 1) * 2
                                    val pos = animT * steps
                                    val step = floor(pos).toInt() % steps
                                    val f = pos - floor(pos)
                                    val ascending = step < n - 1
                                    val from = if (ascending) step else steps - step
                                    val to = if (ascending) from + 1 else from - 1
                                    val rawBlend = if (f < 0.72f) 0f else ((f - 0.72f) / 0.28f).coerceIn(0f, 1f)
                                    // Smooth easeInOut curve
                                    val blend = rawBlend * rawBlend * (3f - 2f * rawBlend)

                                    val alphaFrom = ((1f - blend) * 245).toInt().coerceIn(0, 255)
                                    if (alphaFrom > 3) {
                                        paint.color = android.graphics.Color.argb(alphaFrom, 226, 238, 255)
                                        nativeCanvas.drawPath(loaded.frames[from.coerceIn(0, n - 1)], paint)
                                    }

                                    val alphaTo = (blend * 245).toInt().coerceIn(0, 255)
                                    if (alphaTo > 3) {
                                        paint.color = android.graphics.Color.argb(alphaTo, 168, 255, 53)
                                        nativeCanvas.drawPath(loaded.frames[to.coerceIn(0, n - 1)], paint)
                                    }
                                }
                                nativeCanvas.restore()
                            }
                        }
                    }

                    // Target Muscle Badge (top-left)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xCC0F172A),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Músculo: ${exercise.muscleGroup}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Frame counter badge (top-end)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xCC0F172A),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        val totalFrames = artData?.frames?.size ?: 1
                        Text(
                            text = if (isConcentric) "↑ Fase Concéntrica ($totalFrames frames)" else "↓ Fase Excéntrica ($totalFrames frames)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isConcentric) NeonLime else PrimaryOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
