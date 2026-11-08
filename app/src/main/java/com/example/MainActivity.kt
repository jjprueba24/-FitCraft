package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FitnessViewModel
import com.example.ui.components.AddMealBottomSheet
import com.example.ui.components.AddMeasurementBottomSheet
import com.example.ui.components.AddProgressPhotoBottomSheet
import com.example.ui.components.RestTimerFloatingBar
import com.example.ui.components.TermsAndPrivacyBottomSheet
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.WorkoutScreen
import com.example.ui.theme.DarkNavyBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonLime

enum class AppNavDestination(val label: String, val iconFilled: androidx.compose.ui.graphics.vector.ImageVector, val iconOutlined: androidx.compose.ui.graphics.vector.ImageVector) {
    WORKOUT("Gym", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    NUTRITION("Dieta", Icons.Filled.Restaurant, Icons.Outlined.Restaurant),
    PROGRESS("Progreso", Icons.Filled.InsertChart, Icons.Outlined.InsertChart),
    PROFILE("Perfil", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {

    private val viewModel: FitnessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                FitCraftApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitCraftApp(viewModel: FitnessViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(AppNavDestination.WORKOUT) }

    // Bottom sheet dialog states
    var showAddMealSheet by remember { mutableStateOf(false) }
    var showAddMeasurementSheet by remember { mutableStateOf(false) }
    var showAddPhotoSheet by remember { mutableStateOf(false) }
    var showTermsAndPrivacySheet by remember { mutableStateOf(false) }
    var showAiMealSuggestionsSheet by remember { mutableStateOf(false) }
    var showMusicPlaylistsSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentDestination) {
                            AppNavDestination.WORKOUT -> "FitCraft • Gym & Música"
                            AppNavDestination.NUTRITION -> "FitCraft • Nutrición & IA"
                            AppNavDestination.PROGRESS -> "FitCraft • Progreso & Fotos"
                            AppNavDestination.PROFILE -> "FitCraft • Mi Perfil y Metas"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showTermsAndPrivacySheet = true },
                        modifier = Modifier.testTag("action_top_terms")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Policy,
                            contentDescription = "Términos y Privacidad",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavyBg,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkNavyBg,
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                AppNavDestination.values().forEach { destination ->
                    val isSelected = destination == currentDestination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.iconFilled else destination.iconOutlined,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0F141C),
                            selectedTextColor = NeonLime,
                            indicatorColor = NeonLime,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppNavDestination.WORKOUT -> {
                    WorkoutScreen(
                        userProfile = state.userProfile,
                        completedExerciseIds = state.completedExercises,
                        currentDayIndex = state.currentDayIndex,
                        currentMusicTrack = state.currentMusicTrack,
                        isMusicPlaying = state.isMusicPlaying,
                        musicProgress = state.musicProgressPercent,
                        onToggleMusicPlay = { viewModel.toggleMusicPlay() },
                        onNextMusicTrack = { viewModel.nextMusicTrack() },
                        onOpenMusicPlaylists = { showMusicPlaylistsSheet = true },
                        onSelectDay = { viewModel.selectDay(it) },
                        onToggleExerciseCompleted = { viewModel.toggleExerciseCompleted(it) },
                        onStartRestTimer = { sec, name -> viewModel.startRestTimer(sec, name) },
                        onOpenProfileSettings = { currentDestination = AppNavDestination.PROFILE }
                    )
                }

                AppNavDestination.NUTRITION -> {
                    NutritionScreen(
                        userProfile = state.userProfile,
                        meals = state.meals,
                        onOpenAddMeal = { showAddMealSheet = true },
                        onOpenAiSuggestions = { showAiMealSuggestionsSheet = true },
                        onDeleteMeal = { viewModel.deleteMeal(it) },
                        onOpenProfileSettings = { currentDestination = AppNavDestination.PROFILE }
                    )
                }

                AppNavDestination.PROGRESS -> {
                    ProgressScreen(
                        userProfile = state.userProfile,
                        measurements = state.measurements,
                        photos = state.photos,
                        selectedMetric = state.selectedProgressMetric,
                        onSelectMetric = { viewModel.selectProgressMetric(it) },
                        onOpenAddMeasurement = { showAddMeasurementSheet = true },
                        onOpenAddPhoto = { showAddPhotoSheet = true },
                        onDeleteMeasurement = { viewModel.deleteMeasurement(it) },
                        onDeletePhoto = { viewModel.deletePhoto(it) }
                    )
                }

                AppNavDestination.PROFILE -> {
                    ProfileScreen(
                        userProfile = state.userProfile,
                        onLoginWithEmail = { email -> viewModel.loginWithEmail(email) },
                        onContinueAsGuest = { viewModel.startGuestSession() },
                        onWipeAndReset = { viewModel.wipeAndResetData() },
                        onSaveProfile = { h, w, tw, goal, act, gen, age ->
                            viewModel.updateUserProfile(h, w, tw, goal, act, gen, age)
                        },
                        onOpenTermsAndPrivacy = { showTermsAndPrivacySheet = true }
                    )
                }
            }

            // Floating rest timer bar when active
            AnimatedVisibility(
                visible = state.restTimerSeconds > 0 || state.isRestTimerRunning,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                RestTimerFloatingBar(
                    secondsRemaining = state.restTimerSeconds,
                    totalSeconds = state.restTimerTotal,
                    isRunning = state.isRestTimerRunning,
                    exerciseName = state.restTimerExerciseName,
                    onPauseResume = { viewModel.pauseResumeTimer() },
                    onReset = { viewModel.resetTimer() },
                    onAdd15Seconds = { viewModel.add15SecondsToTimer() },
                    onDismiss = { viewModel.dismissTimer() }
                )
            }
        }
    }

    // Modal Bottom Sheets
    if (showAddMealSheet) {
        AddMealBottomSheet(
            onDismiss = { showAddMealSheet = false },
            onSaveMeal = { viewModel.addMeal(it) }
        )
    }

    if (showAddMeasurementSheet) {
        AddMeasurementBottomSheet(
            lastWeightKg = state.userProfile.currentWeightKg,
            onDismiss = { showAddMeasurementSheet = false },
            onSaveMeasurement = { viewModel.addMeasurement(it) }
        )
    }

    if (showAddPhotoSheet) {
        AddProgressPhotoBottomSheet(
            currentWeightKg = state.userProfile.currentWeightKg,
            onDismiss = { showAddPhotoSheet = false },
            onSavePhoto = { viewModel.addPhoto(it) }
        )
    }

    if (showTermsAndPrivacySheet) {
        TermsAndPrivacyBottomSheet(
            onDismiss = { showTermsAndPrivacySheet = false }
        )
    }

    if (showAiMealSuggestionsSheet) {
        val totalCalories = state.meals.sumOf { it.calories }
        val totalProtein = state.meals.sumOf { it.proteinGrams.toDouble() }.toFloat()
        val totalCarbs = state.meals.sumOf { it.carbsGrams.toDouble() }.toFloat()
        val totalFat = state.meals.sumOf { it.fatGrams.toDouble() }.toFloat()

        val remCals = state.userProfile.targetCalories - totalCalories
        val remProtein = state.userProfile.targetProteinGrams.toFloat() - totalProtein
        val remCarbs = state.userProfile.targetCarbsGrams.toFloat() - totalCarbs
        val remFat = state.userProfile.targetFatGrams.toFloat() - totalFat

        com.example.ui.components.AiMealSuggestionsBottomSheet(
            userProfile = state.userProfile,
            remainingCalories = remCals,
            remainingProtein = remProtein,
            remainingCarbs = remCarbs,
            remainingFat = remFat,
            onAcceptMeal = { viewModel.addMeal(it) },
            onDismiss = { showAiMealSuggestionsSheet = false }
        )
    }

    if (showMusicPlaylistsSheet) {
        com.example.ui.components.MusicPlaylistsBottomSheet(
            currentTrackId = state.currentMusicTrack.id,
            isPlaying = state.isMusicPlaying,
            onSelectTrack = { viewModel.selectMusicTrack(it) },
            onDismiss = { showMusicPlaylistsSheet = false }
        )
    }
}
