package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Exercise
import com.example.data.FitnessGoal
import com.example.data.UserProfile
import com.example.data.WorkoutPlanPresets
import com.example.ui.components.ExerciseCard
import com.example.ui.components.InteractiveBodyMapCard
import com.example.ui.components.MuscleZone
import com.example.ui.components.matchesMuscleZone
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange

@Composable
fun WorkoutScreen(
    userProfile: UserProfile,
    completedExerciseIds: Set<String>,
    customExercises: List<Exercise> = emptyList(),
    currentDayIndex: Int,
    currentMusicTrack: com.example.data.MusicTrack,
    spotifyState: com.example.data.SpotifyConnectionState,
    isMusicPlaying: Boolean,
    musicProgress: Float,
    onToggleMusicPlay: () -> Unit,
    onPreviousMusicTrack: () -> Unit,
    onNextMusicTrack: () -> Unit,
    onConnectSpotify: () -> Unit,
    onOpenMusicPlaylists: () -> Unit,
    onOpenGymManeTools: () -> Unit = {},
    onSelectDay: (Int) -> Unit,
    onToggleExerciseCompleted: (String) -> Unit,
    onStartRestTimer: (Int, String) -> Unit,
    onOpenProfileSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routine = WorkoutPlanPresets.getRoutineFor(userProfile.goal)
    val selectedDay = routine.days.getOrNull(currentDayIndex) ?: routine.days.first()
    var selectedMuscleZone by remember { mutableStateOf(MuscleZone.ALL) }

    val combinedDayExercises = remember(selectedDay, customExercises) {
        if (selectedDay.isRestDay) customExercises else selectedDay.exercises + customExercises
    }

    val filteredExercises = remember(combinedDayExercises, selectedMuscleZone) {
        combinedDayExercises.filter { it.matchesMuscleZone(selectedMuscleZone) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("workout_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Integrated Music Player Bar (100% In-App Spotify Embed + Media3 ExoPlayer)
        item {
            com.example.ui.components.WorkoutMiniMusicPlayer(
                currentTrack = currentMusicTrack,
                spotifyState = spotifyState,
                isPlaying = isMusicPlaying,
                progressPercent = musicProgress,
                onTogglePlay = onToggleMusicPlay,
                onPreviousTrack = onPreviousMusicTrack,
                onNextTrack = onNextMusicTrack,
                onConnectSpotify = onConnectSpotify,
                onOpenPlaylistSheet = onOpenMusicPlaylists
            )
        }

        // Hero Routine Banner with Goal, Equipment, Days/Week & GymMane Tools Shortcut
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("routine_header_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (userProfile.goal == FitnessGoal.LOSE_WEIGHT) AccentCyan.copy(alpha = 0.2f) else PrimaryOrange.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.FlashOn,
                                            contentDescription = null,
                                            tint = if (userProfile.goal == FitnessGoal.LOSE_WEIGHT) AccentCyan else PrimaryOrange,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = userProfile.goal.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (userProfile.goal == FitnessGoal.LOSE_WEIGHT) AccentCyan else PrimaryOrange
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonLime.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "${userProfile.equipment} • ${userProfile.trainingDaysPerWeek}d/sem",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonLime,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onOpenProfileSettings,
                                modifier = Modifier.size(34.dp).testTag("btn_change_profile_goal")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = "Ajustar objetivo y equipo",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = routine.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Calculado para: ${userProfile.heightCm.toInt()} cm • ${userProfile.currentWeightKg} kg → Meta ${userProfile.targetWeightKg} kg (${userProfile.targetCalories} kcal/día)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // GymMane Pro Tools Quick Launch Bar
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentCyan.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenGymManeTools() }
                                .testTag("btn_open_gymmane_tools")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Calculate,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Suite GymMane: Discos Barra • Calc 1RM • +Ejercicio • Pase QR",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AccentCyan
                                    )
                                }
                                Text(
                                    text = "ABRIR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonLime
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Front & Back Anatomical Body Map (GymMane)
        item {
            InteractiveBodyMapCard(
                exercisesForDay = combinedDayExercises,
                completedExerciseIds = completedExerciseIds,
                selectedZone = selectedMuscleZone,
                onSelectZone = { selectedMuscleZone = it }
            )
        }

        // Days of week selector tabs
        item {
            Text(
                text = "Días de Entrenamiento y Descanso",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(routine.days) { index, day ->
                    val isSelected = index == currentDayIndex
                    val dayExercises = day.exercises
                    val completedCount = dayExercises.count { completedExerciseIds.contains(it.id) }
                    val allDone = dayExercises.isNotEmpty() && completedCount == dayExercises.size

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            day.isRestDay -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null,
                        modifier = Modifier
                            .width(88.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectDay(index) }
                            .testTag("day_tab_$index")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.dayName.take(3).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            if (day.isRestDay) {
                                Icon(
                                    imageVector = Icons.Filled.Bedtime,
                                    contentDescription = "Descanso",
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Relax",
                                    fontSize = 10.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Icon(
                                    imageVector = if (allDone) Icons.Filled.CheckCircle else Icons.Filled.FitnessCenter,
                                    contentDescription = "Entreno",
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (allDone) NeonLime else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "$completedCount/${dayExercises.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Title & Progress Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${selectedDay.dayName}: ${selectedDay.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (selectedDay.isRestDay && customExercises.isEmpty()) {
                        Text(
                            text = "Día de descanso programado: la recuperación y el sueño reparador son cuando el cuerpo quema grasa y construye fibras.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val count = filteredExercises.count { completedExerciseIds.contains(it.id) }
                        Text(
                            text = "Mostrando ${filteredExercises.size} ejercicios (${count} completados hoy)",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonLime,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Exercises List with Animated Figures + Real Technique Photos + GymMane Set Logger
        items(filteredExercises, key = { it.id }) { exercise ->
            val isDone = completedExerciseIds.contains(exercise.id)
            ExerciseCard(
                exercise = exercise,
                isCompleted = isDone,
                onToggleCompleted = { onToggleExerciseCompleted(exercise.id) },
                onStartRestTimer = onStartRestTimer
            )
        }
    }
}
