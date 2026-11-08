package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CheckCircleOutline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Exercise
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CardBorder
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange

@Composable
fun ExerciseCard(
    exercise: Exercise,
    isCompleted: Boolean,
    onToggleCompleted: () -> Unit,
    onStartRestTimer: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedTip by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isCompleted) NeonLime.copy(alpha = 0.35f) else CardBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("exercise_card_${exercise.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // 1. Header Row: Checkbox, Title & Muscle Badge, Rest Timer Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleCompleted,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("toggle_exercise_${exercise.id}")
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Completado",
                            tint = NeonLime,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircleOutline,
                            contentDescription = "Marcar completado",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = exercise.muscleGroup,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (exercise.restSeconds > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryOrange.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onStartRestTimer(exercise.restSeconds, exercise.name) }
                            .testTag("rest_timer_${exercise.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = "Descanso",
                                tint = PrimaryOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${exercise.restSeconds}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryOrange
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. FULL-WIDTH INTERACTIVE BIOMECHANICAL ANIMATION STAGE ON EVERY EXERCISE CARD
            ExerciseAnimationStage(exercise = exercise)

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Always-visible Technique Tip & Common Mistakes Summary + Series Logger Toggle
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "💡 Cómo hacerlo: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonLime
                        )
                        Text(
                            text = exercise.tip,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (exercise.commonMistakes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "⚠️ Evita: ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryOrange
                            )
                            Text(
                                text = exercise.commonMistakes,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Sets / Reps Badges + Expandable Series & 1RM Logger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoBadge(label = "Series", value = "${exercise.sets} sets")
                InfoBadge(label = "Reps", value = exercise.reps)
                if (exercise.restSeconds > 0) {
                    InfoBadge(label = "Descanso", value = "${exercise.restSeconds}s")
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonLime.copy(alpha = 0.16f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expandedTip = !expandedTip }
                        .testTag("btn_toggle_sets_${exercise.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Registrar series y 1RM",
                            tint = NeonLime,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (expandedTip) "Ocultar Series" else "Registrar Series & 1RM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonLime
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expandedTip) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    GymManeExerciseSetLogger(
                        exercise = exercise,
                        onAllSetsCompleted = {
                            if (!isCompleted) onToggleCompleted()
                        },
                        onStartRestTimer = {
                            if (exercise.restSeconds > 0) {
                                onStartRestTimer(exercise.restSeconds, exercise.name)
                            }
                        }
                    )
                }
            }
        }
    }
}

private enum class SetKind(val code: String, val label: String, val badgeColor: Color) {
    WARMUP("W", "Calentamiento", Color(0xFFF59E0B)),
    NORMAL("N", "Efectiva", Color(0xFFA8FF35)),
    DROP("D", "Drop Set", Color(0xFF00E5FF)),
    FAILURE("F", "Al Fallo", Color(0xFFFF6B35)),
    SUPERSET("S", "Superserie", Color(0xFFC084FC));

    fun next(): SetKind {
        val all = values()
        return all[(ordinal + 1) % all.size]
    }
}

private data class SetEntryState(
    val setNumber: Int,
    val kind: SetKind,
    val weightKg: Float,
    val reps: Int,
    val done: Boolean = false
)

@Composable
private fun GymManeExerciseSetLogger(
    exercise: Exercise,
    onAllSetsCompleted: () -> Unit,
    onStartRestTimer: () -> Unit
) {
    val defaultReps = remember(exercise.reps) {
        Regex("\\d+").find(exercise.reps)?.value?.toIntOrNull()?.coerceIn(1, 30) ?: 10
    }
    val defaultWeight = remember(exercise.muscleGroup) {
        when {
            exercise.muscleGroup.contains("Pierna", true) || exercise.muscleGroup.contains("Cuádriceps", true) -> 60f
            exercise.muscleGroup.contains("Pecho", true) || exercise.muscleGroup.contains("Espalda", true) -> 50f
            else -> 20f
        }
    }

    var setsList by remember(exercise.id) {
        mutableStateOf(
            (1..exercise.sets.coerceAtLeast(3)).map { idx ->
                SetEntryState(
                    setNumber = idx,
                    kind = if (idx == 1) SetKind.WARMUP else SetKind.NORMAL,
                    weightKg = if (idx == 1) (defaultWeight * 0.7f).toInt().toFloat() else defaultWeight,
                    reps = defaultReps,
                    done = false
                )
            }
        )
    }

    val best1Rm = setsList.maxOfOrNull { s ->
        if (s.reps == 1) s.weightKg else s.weightKg * (1f + s.reps / 30f)
    }?.toInt() ?: 0

    val totalVolumeKg = setsList.filter { it.done }.sumOf { (it.weightKg * it.reps).toInt() }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0B1018),
        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REGISTRO DE SERIES PRO (Toca W/N/D/F)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentCyan
                )
                Text(
                    text = "1RM Est: $best1Rm kg • Vol: $totalVolumeKg kg",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonLime
                )
            }

            setsList.forEachIndexed { idx, item ->
                val est1Rm = (if (item.reps == 1) item.weightKg else item.weightKg * (1f + item.reps / 30f)).toInt()
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.done) NeonLime.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = item.kind.badgeColor.copy(alpha = 0.22f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    setsList = setsList.toMutableList().also {
                                        it[idx] = item.copy(kind = item.kind.next())
                                    }
                                }
                        ) {
                            Text(
                                text = "${item.setNumber}•${item.kind.code}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = item.kind.badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "−",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryOrange,
                                modifier = Modifier
                                    .clickable {
                                        setsList = setsList.toMutableList().also {
                                            it[idx] = item.copy(weightKg = (item.weightKg - 2.5f).coerceAtLeast(0f))
                                        }
                                    }
                                    .padding(horizontal = 6.dp)
                            )
                            Text(
                                text = "${item.weightKg} kg",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "+",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonLime,
                                modifier = Modifier
                                    .clickable {
                                        setsList = setsList.toMutableList().also {
                                            it[idx] = item.copy(weightKg = item.weightKg + 2.5f)
                                        }
                                    }
                                    .padding(horizontal = 6.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "−",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryOrange,
                                modifier = Modifier
                                    .clickable {
                                        setsList = setsList.toMutableList().also {
                                            it[idx] = item.copy(reps = (item.reps - 1).coerceAtLeast(1))
                                        }
                                    }
                                    .padding(horizontal = 5.dp)
                            )
                            Text(
                                text = "${item.reps} reps",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "+",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonLime,
                                modifier = Modifier
                                    .clickable {
                                        setsList = setsList.toMutableList().also {
                                            it[idx] = item.copy(reps = item.reps + 1)
                                        }
                                    }
                                    .padding(horizontal = 5.dp)
                            )
                        }

                        Text(
                            text = "1RM:$est1Rm",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.done) NeonLime else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val nextDone = !item.done
                                    val updated = setsList.toMutableList().also {
                                        it[idx] = item.copy(done = nextDone)
                                    }
                                    setsList = updated
                                    if (nextDone) {
                                        onStartRestTimer()
                                        if (updated.all { it.done }) {
                                            onAllSetsCompleted()
                                        }
                                    }
                                }
                        ) {
                            Text(
                                text = if (item.done) "✓" else "OK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.done) Color(0xFF0F141C) else NeonLime,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
