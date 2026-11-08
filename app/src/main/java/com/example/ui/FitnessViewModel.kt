package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityLevel
import com.example.data.BodyMeasurement
import com.example.data.FitnessGoal
import com.example.data.FitnessRepository
import com.example.data.Gender
import com.example.data.MealItem
import com.example.data.ProgressPhoto
import com.example.data.UserProfile
import com.example.data.WorkoutPlanPresets
import com.example.data.WorkoutRoutine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProfileAndData(
    val profile: UserProfile,
    val meals: List<MealItem>,
    val measurements: List<BodyMeasurement>,
    val photos: List<ProgressPhoto>,
    val completed: Set<String>
)

data class FitnessUiState(
    val userProfile: UserProfile = UserProfile(),
    val meals: List<MealItem> = emptyList(),
    val measurements: List<BodyMeasurement> = emptyList(),
    val photos: List<ProgressPhoto> = emptyList(),
    val completedExercises: Set<String> = emptySet(),
    val currentDayIndex: Int = 0,
    val selectedProgressMetric: String = "Peso (kg)",
    val restTimerSeconds: Int = 0,
    val restTimerTotal: Int = 0,
    val isRestTimerRunning: Boolean = false,
    val restTimerExerciseName: String = "",
    val currentMusicTrack: com.example.data.MusicTrack = com.example.data.MusicLibrary.tracks.first(),
    val isMusicPlaying: Boolean = false,
    val musicProgressPercent: Float = 0.25f
)

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FitnessRepository(application)

    private val _currentDayIndex = MutableStateFlow(0)
    private val _selectedProgressMetric = MutableStateFlow("Peso (kg)")

    // Rest timer state
    private val _restTimerSeconds = MutableStateFlow(0)
    private val _restTimerTotal = MutableStateFlow(0)
    private val _isRestTimerRunning = MutableStateFlow(false)
    private val _restTimerExerciseName = MutableStateFlow("")
    private var timerJob: Job? = null

    // Music player state
    private val _currentMusicTrack = MutableStateFlow(com.example.data.MusicLibrary.tracks.first())
    private val _isMusicPlaying = MutableStateFlow(false)
    private val _musicProgress = MutableStateFlow(0.15f)
    private var musicJob: Job? = null

    val uiState: StateFlow<FitnessUiState> = combine(
        combine(
            repository.userProfileFlow,
            repository.mealsFlow,
            repository.measurementsFlow,
            repository.photosFlow,
            repository.completedExercisesFlow
        ) { profile, meals, measurements, photos, completed ->
            ProfileAndData(profile, meals, measurements, photos, completed)
        },
        _currentDayIndex,
        _selectedProgressMetric,
        _restTimerSeconds,
        _isRestTimerRunning
    ) { data, dayIdx, metric, timerSec, isTimerRunning ->
        FitnessUiState(
            userProfile = data.profile,
            meals = data.meals,
            measurements = data.measurements,
            photos = data.photos,
            completedExercises = data.completed,
            currentDayIndex = dayIdx,
            selectedProgressMetric = metric,
            restTimerSeconds = timerSec,
            restTimerTotal = _restTimerTotal.value,
            isRestTimerRunning = isTimerRunning,
            restTimerExerciseName = _restTimerExerciseName.value,
            currentMusicTrack = _currentMusicTrack.value,
            isMusicPlaying = _isMusicPlaying.value,
            musicProgressPercent = _musicProgress.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FitnessUiState()
    )

    fun updateUserProfile(
        heightCm: Float,
        currentWeightKg: Float,
        targetWeightKg: Float,
        goal: FitnessGoal,
        activityLevel: ActivityLevel,
        gender: Gender,
        age: Int
    ) {
        viewModelScope.launch {
            val updated = uiState.value.userProfile.copy(
                heightCm = heightCm,
                currentWeightKg = currentWeightKg,
                targetWeightKg = targetWeightKg,
                goal = goal,
                activityLevel = activityLevel,
                gender = gender,
                age = age
            )
            repository.saveUserProfile(updated)

            // If user's latest measurement has different weight, update or append
            val currentMeasurements = uiState.value.measurements
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            val latest = currentMeasurements.lastOrNull()
            if (latest == null || latest.weightKg != currentWeightKg) {
                repository.addMeasurement(
                    BodyMeasurement(
                        dateEpochMillis = System.currentTimeMillis(),
                        dateFormatted = sdf.format(Date()),
                        weightKg = currentWeightKg,
                        notes = "Ajuste de perfil"
                    )
                )
            }
        }
    }

    fun selectDay(index: Int) {
        _currentDayIndex.value = index
    }

    fun selectProgressMetric(metric: String) {
        _selectedProgressMetric.value = metric
    }

    fun toggleExerciseCompleted(exerciseId: String) {
        viewModelScope.launch {
            repository.toggleExerciseCompleted(exerciseId)
        }
    }

    // Timer controls
    fun startRestTimer(seconds: Int, exerciseName: String) {
        timerJob?.cancel()
        _restTimerTotal.value = seconds
        _restTimerSeconds.value = seconds
        _restTimerExerciseName.value = exerciseName
        _isRestTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_restTimerSeconds.value > 0 && _isRestTimerRunning.value) {
                delay(1000)
                if (_isRestTimerRunning.value) {
                    _restTimerSeconds.value -= 1
                }
            }
            if (_restTimerSeconds.value <= 0) {
                _isRestTimerRunning.value = false
            }
        }
    }

    fun pauseResumeTimer() {
        _isRestTimerRunning.value = !_isRestTimerRunning.value
        if (_isRestTimerRunning.value && timerJob?.isActive != true) {
            timerJob = viewModelScope.launch {
                while (_restTimerSeconds.value > 0 && _isRestTimerRunning.value) {
                    delay(1000)
                    if (_isRestTimerRunning.value) {
                        _restTimerSeconds.value -= 1
                    }
                }
                if (_restTimerSeconds.value <= 0) {
                    _isRestTimerRunning.value = false
                }
            }
        }
    }

    fun resetTimer() {
        _restTimerSeconds.value = _restTimerTotal.value
    }

    fun add15SecondsToTimer() {
        _restTimerSeconds.value += 15
        _restTimerTotal.value += 15
    }

    fun dismissTimer() {
        timerJob?.cancel()
        _isRestTimerRunning.value = false
        _restTimerSeconds.value = 0
    }

    // Music Player controls
    fun toggleMusicPlay() {
        _isMusicPlaying.value = !_isMusicPlaying.value
        if (_isMusicPlaying.value) {
            startMusicProgressLoop()
        } else {
            musicJob?.cancel()
        }
    }

    fun selectMusicTrack(track: com.example.data.MusicTrack) {
        _currentMusicTrack.value = track
        _musicProgress.value = 0f
        _isMusicPlaying.value = true
        startMusicProgressLoop()
    }

    fun nextMusicTrack() {
        val tracks = com.example.data.MusicLibrary.tracks
        val currentIndex = tracks.indexOfFirst { it.id == _currentMusicTrack.value.id }
        val nextIndex = (currentIndex + 1) % tracks.size
        selectMusicTrack(tracks[nextIndex])
    }

    private fun startMusicProgressLoop() {
        musicJob?.cancel()
        musicJob = viewModelScope.launch {
            while (_isMusicPlaying.value) {
                delay(1000)
                val current = _musicProgress.value
                if (current >= 1f) {
                    nextMusicTrack()
                } else {
                    _musicProgress.value = (current + 0.015f).coerceAtMost(1f)
                }
            }
        }
    }

    // Meals actions
    fun addMeal(meal: MealItem) {
        viewModelScope.launch {
            repository.addMeal(meal)
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            repository.deleteMeal(mealId)
        }
    }

    // Measurement actions
    fun addMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch {
            repository.addMeasurement(measurement)
            // also update current weight in user profile
            val currentProfile = uiState.value.userProfile
            repository.saveUserProfile(currentProfile.copy(currentWeightKg = measurement.weightKg))
        }
    }

    fun deleteMeasurement(id: String) {
        viewModelScope.launch {
            repository.deleteMeasurement(id)
        }
    }

    // Photo actions
    fun addPhoto(photo: ProgressPhoto) {
        viewModelScope.launch {
            repository.addPhoto(photo)
        }
    }

    fun deletePhoto(id: String) {
        viewModelScope.launch {
            repository.deletePhoto(id)
        }
    }

    // Account & 4-Hour Guest Session
    fun loginWithEmail(email: String) {
        viewModelScope.launch {
            repository.loginWithEmail(email)
        }
    }

    fun startGuestSession() {
        viewModelScope.launch {
            repository.startGuestSession()
        }
    }

    fun wipeAndResetData() {
        viewModelScope.launch {
            repository.wipeAllUserData()
        }
    }

    fun getActiveWorkoutRoutine(): WorkoutRoutine {
        return WorkoutPlanPresets.getRoutineFor(uiState.value.userProfile.goal)
    }
}
