package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccountAuthStatus
import com.example.data.ActivityLevel
import com.example.data.BodyMeasurement
import com.example.data.FitnessGoal
import com.example.data.FitnessRepository
import com.example.data.Gender
import com.example.data.MealItem
import com.example.data.MealType
import com.example.data.MusicLibrary
import com.example.data.MusicTrack
import com.example.data.ProgressPhoto
import com.example.data.SpotifyConnectionState
import com.example.data.SpotifyFitnessPlaylist
import com.example.data.UserProfile
import com.example.data.UserSheetManager
import com.example.data.WorkoutPlanPresets
import com.example.data.WorkoutRoutine
import com.example.data.spotify.SpotifySdkConnector
import com.example.ui.components.RecipeIngredient
import com.example.ui.components.SuggestedMeal
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ProfileAndData(
    val profile: UserProfile,
    val meals: List<MealItem>,
    val measurements: List<BodyMeasurement>,
    val photos: List<ProgressPhoto>,
    val completed: Set<String>
)

data class MusicAndTimerState(
    val currentTrack: MusicTrack,
    val isMusicPlaying: Boolean,
    val musicProgress: Float,
    val spotifyState: SpotifyConnectionState,
    val restTimerSeconds: Int,
    val restTimerTotal: Int,
    val isRestTimerRunning: Boolean,
    val restTimerExerciseName: String
)

data class GeminiRecipeState(
    val recipes: List<SuggestedMeal> = emptyList(),
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)

data class FitnessUiState(
    val userProfile: UserProfile = UserProfile(),
    val meals: List<MealItem> = emptyList(),
    val measurements: List<BodyMeasurement> = emptyList(),
    val photos: List<ProgressPhoto> = emptyList(),
    val completedExercises: Set<String> = emptySet(),
    val customExercises: List<com.example.data.Exercise> = emptyList(),
    val currentDayIndex: Int = 0,
    val selectedProgressMetric: String = "Peso (kg)",
    val restTimerSeconds: Int = 0,
    val restTimerTotal: Int = 0,
    val isRestTimerRunning: Boolean = false,
    val restTimerExerciseName: String = "",
    val currentMusicTrack: MusicTrack = MusicLibrary.tracks.first(),
    val isMusicPlaying: Boolean = false,
    val musicProgressPercent: Float = 0.15f,
    val spotifyState: SpotifyConnectionState = SpotifyConnectionState(),
    val authStatus: AccountAuthStatus = AccountAuthStatus(),
    val geminiRecipes: List<SuggestedMeal> = emptyList(),
    val isGeneratingGeminiRecipe: Boolean = false,
    val geminiErrorMessage: String? = null
)

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FitnessRepository(application)
    private val spotifyConnector = SpotifySdkConnector(application, viewModelScope)
    private val userSheetManager = UserSheetManager(application)

    private val _currentDayIndex = MutableStateFlow(0)
    private val _selectedProgressMetric = MutableStateFlow("Peso (kg)")
    private val _customExercises = MutableStateFlow<List<com.example.data.Exercise>>(emptyList())

    // Account & Sheet verification state
    private val _authStatus = MutableStateFlow(
        run {
            val activeEmail = userSheetManager.getActiveEmail()
            if (!activeEmail.isNullOrBlank()) {
                val sheetFile = userSheetManager.getSheetFileForEmail(activeEmail)
                AccountAuthStatus(
                    isLoggedIn = true,
                    email = activeEmail,
                    isEmailVerified = true,
                    sheetFileName = sheetFile.name,
                    lastSheetSyncTime = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date())
                )
            } else {
                AccountAuthStatus()
            }
        }
    )

    // Gemini AI Recipe Generator state
    private val _geminiState = MutableStateFlow(GeminiRecipeState())

    // Rest timer state
    private val _restTimerSeconds = MutableStateFlow(0)
    private val _restTimerTotal = MutableStateFlow(0)
    private val _isRestTimerRunning = MutableStateFlow(false)
    private val _restTimerExerciseName = MutableStateFlow("")
    private var timerJob: Job? = null

    // Music player state
    private val _currentMusicTrack = MutableStateFlow(MusicLibrary.tracks.first())
    private val _isMusicPlaying = MutableStateFlow(false)
    private val _musicProgress = MutableStateFlow(0.15f)
    private var musicJob: Job? = null

    private val profileAndDataFlow = combine(
        repository.userProfileFlow,
        repository.mealsFlow,
        repository.measurementsFlow,
        repository.photosFlow,
        repository.completedExercisesFlow
    ) { profile, meals, measurements, photos, completed ->
        ProfileAndData(profile, meals, measurements, photos, completed)
    }

    private val musicAndTimerFlow = combine(
        _currentMusicTrack,
        _isMusicPlaying,
        _musicProgress,
        spotifyConnector.connectionState,
        combine(_restTimerSeconds, _isRestTimerRunning) { sec, running -> sec to running }
    ) { track, playing, progress, spotify, (timerSec, timerRunning) ->
        MusicAndTimerState(
            currentTrack = track,
            isMusicPlaying = playing,
            musicProgress = progress,
            spotifyState = spotify,
            restTimerSeconds = timerSec,
            restTimerTotal = _restTimerTotal.value,
            isRestTimerRunning = timerRunning,
            restTimerExerciseName = _restTimerExerciseName.value
        )
    }

    private val authAndGeminiFlow = combine(
        _authStatus,
        _geminiState,
        _customExercises
    ) { auth, gemini, customEx ->
        Triple(auth, gemini, customEx)
    }

    val uiState: StateFlow<FitnessUiState> = combine(
        profileAndDataFlow,
        _currentDayIndex,
        _selectedProgressMetric,
        musicAndTimerFlow,
        authAndGeminiFlow
    ) { data, dayIdx, metric, media, (auth, gemini, customEx) ->
        val effectiveEmail = auth.email ?: data.profile.email
        val sheetFile = userSheetManager.getSheetFileForEmail(effectiveEmail)
        val totalRows = 1 + data.measurements.size + data.meals.size + data.photos.size

        FitnessUiState(
            userProfile = data.profile,
            meals = data.meals,
            measurements = data.measurements,
            photos = data.photos,
            completedExercises = data.completed,
            customExercises = customEx,
            currentDayIndex = dayIdx,
            selectedProgressMetric = metric,
            restTimerSeconds = media.restTimerSeconds,
            restTimerTotal = media.restTimerTotal,
            isRestTimerRunning = media.isRestTimerRunning,
            restTimerExerciseName = media.restTimerExerciseName,
            currentMusicTrack = media.currentTrack,
            isMusicPlaying = media.isMusicPlaying,
            musicProgressPercent = media.musicProgress,
            spotifyState = media.spotifyState,
            authStatus = auth.copy(
                isLoggedIn = !effectiveEmail.isNullOrBlank(),
                email = effectiveEmail,
                sheetFileName = sheetFile.name,
                totalRowsInSheet = totalRows
            ),
            geminiRecipes = gemini.recipes,
            isGeneratingGeminiRecipe = gemini.isGenerating,
            geminiErrorMessage = gemini.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FitnessUiState()
    )

    fun addCustomExercise(exercise: com.example.data.Exercise) {
        _customExercises.value = listOf(exercise) + _customExercises.value
    }

    init {
        // Keep the user's Sheet automatically updated whenever profile, measurements, meals, or photos change
        viewModelScope.launch {
            profileAndDataFlow.collect { data ->
                val activeEmail = _authStatus.value.email ?: data.profile.email
                if (!activeEmail.isNullOrBlank()) {
                    userSheetManager.saveAccountSnapshotAndSheet(
                        email = activeEmail,
                        profile = data.profile,
                        measurements = data.measurements,
                        meals = data.meals,
                        photos = data.photos,
                        completedExercises = data.completed
                    )
                }
            }
        }
    }

    private fun syncCurrentStateToUserSheet(overrideProfile: UserProfile? = null) {
        val current = uiState.value
        val profile = overrideProfile ?: current.userProfile
        val email = profile.email ?: _authStatus.value.email ?: "invitado"
        val file = userSheetManager.saveAccountSnapshotAndSheet(
            email = email,
            profile = profile,
            measurements = current.measurements,
            meals = current.meals,
            photos = current.photos,
            completedExercises = current.completedExercises
        )
        val nowStr = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date())
        _authStatus.value = _authStatus.value.copy(
            sheetFileName = file.name,
            lastSheetSyncTime = nowStr,
            totalRowsInSheet = 1 + current.measurements.size + current.meals.size + current.photos.size
        )
    }

    fun updateUserProfile(
        heightCm: Float,
        currentWeightKg: Float,
        targetWeightKg: Float,
        goal: FitnessGoal,
        activityLevel: ActivityLevel,
        gender: Gender,
        age: Int,
        trainingDaysPerWeek: Int = 5,
        equipment: String = "Gimnasio Completo"
    ) {
        viewModelScope.launch {
            val updated = uiState.value.userProfile.copy(
                heightCm = heightCm,
                currentWeightKg = currentWeightKg,
                targetWeightKg = targetWeightKg,
                goal = goal,
                activityLevel = activityLevel,
                gender = gender,
                age = age,
                trainingDaysPerWeek = trainingDaysPerWeek,
                equipment = equipment
            )
            repository.saveUserProfile(updated)

            val currentMeasurements = uiState.value.measurements
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            val latest = currentMeasurements.lastOrNull()
            if (latest == null || latest.weightKg != currentWeightKg) {
                repository.addMeasurement(
                    BodyMeasurement(
                        dateEpochMillis = System.currentTimeMillis(),
                        dateFormatted = sdf.format(Date()),
                        weightKg = currentWeightKg,
                        notes = "Ajuste de perfil y metas (${goal.label})"
                    )
                )
            }
            syncCurrentStateToUserSheet(overrideProfile = updated)
        }
    }

    // Account Email Verification + PIN + Sheet Restore
    fun requestEmailVerification(email: String, pin: String) {
        viewModelScope.launch {
            val status = userSheetManager.initiateEmailVerification(email, pin)
            _authStatus.value = status

            // If account was already verified and PIN matched, restore their saved Sheet snapshot into Room!
            if (status.isLoggedIn && !status.email.isNullOrBlank()) {
                val savedSnapshot = userSheetManager.loadAccountSnapshot(status.email)
                if (savedSnapshot != null) {
                    repository.restoreAccountSnapshot(savedSnapshot)
                } else {
                    repository.loginWithEmail(status.email)
                    syncCurrentStateToUserSheet()
                }
            }
        }
    }

    fun confirmVerificationCode(email: String, code: String) {
        viewModelScope.launch {
            val current = uiState.value
            val status = userSheetManager.confirmEmailVerificationCode(
                email = email,
                enteredCode = code,
                currentProfile = current.userProfile,
                measurements = current.measurements,
                meals = current.meals,
                photos = current.photos,
                completedExercises = current.completedExercises
            )
            _authStatus.value = status
            if (status.isLoggedIn && !status.email.isNullOrBlank()) {
                val savedSnapshot = userSheetManager.loadAccountSnapshot(status.email)
                if (savedSnapshot != null) {
                    repository.restoreAccountSnapshot(savedSnapshot)
                } else {
                    repository.loginWithEmail(status.email)
                }
            }
        }
    }

    fun logoutPreservingData() {
        viewModelScope.launch {
            // Save snapshot to Sheet first so zero progress is lost
            syncCurrentStateToUserSheet()
            val status = userSheetManager.logoutPreservingSheetData()
            _authStatus.value = status
            repository.logoutSessionPreservingData()
        }
    }

    fun exportUserSheetCsv() {
        val current = uiState.value
        val email = current.userProfile.email ?: _authStatus.value.email ?: "invitado"
        userSheetManager.saveAccountSnapshotAndSheet(
            email = email,
            profile = current.userProfile,
            measurements = current.measurements,
            meals = current.meals,
            photos = current.photos,
            completedExercises = current.completedExercises
        )
        userSheetManager.shareOrExportSheetCsv(email)
    }

    /**
     * Generates a custom Peruvian market recipe using Firebase AI Logic (`gemini-3.8-flash`)
     * tailored to the user's remaining calories, protein, carbs, and fat.
     */
    fun generatePeruvianRecipeWithGemini(selectedIngredients: List<String>) {
        if (_geminiState.value.isGenerating) return

        val state = uiState.value
        val totalCalories = state.meals.sumOf { it.calories }
        val totalProtein = state.meals.sumOf { it.proteinGrams.toDouble() }.toFloat()
        val totalCarbs = state.meals.sumOf { it.carbsGrams.toDouble() }.toFloat()
        val totalFat = state.meals.sumOf { it.fatGrams.toDouble() }.toFloat()

        val remCalories = (state.userProfile.targetCalories - totalCalories).coerceIn(250, 950)
        val remProtein = (state.userProfile.targetProteinGrams.toFloat() - totalProtein).coerceIn(22f, 65f)
        val remCarbs = (state.userProfile.targetCarbsGrams.toFloat() - totalCarbs).coerceIn(20f, 110f)
        val remFat = (state.userProfile.targetFatGrams.toFloat() - totalFat).coerceIn(8f, 35f)

        val ingredientsText = if (selectedIngredients.isEmpty()) {
            "palta, camote, quinua, pescado bonito o tilapia, pechuga de pollo"
        } else {
            selectedIngredients.joinToString(", ")
        }

        _geminiState.value = _geminiState.value.copy(isGenerating = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val recipeSchema = Schema.obj(
                    mapOf(
                        "title" to Schema.string("Nombre del plato peruano fitness"),
                        "description" to Schema.string("Descripción breve y apetitosa del plato"),
                        "prepTimeMinutes" to Schema.integer("Tiempo de preparación en minutos"),
                        "calories" to Schema.integer("Calorías totales de la receta"),
                        "proteinGrams" to Schema.double("Gramos de proteína"),
                        "carbsGrams" to Schema.double("Gramos de carbohidratos"),
                        "fatGrams" to Schema.double("Gramos de grasas saludables"),
                        "mealType" to Schema.enumeration(listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")),
                        "reasonForGoal" to Schema.string("Por qué ayuda al objetivo del usuario"),
                        "ingredients" to Schema.array(
                            Schema.obj(
                                mapOf(
                                    "name" to Schema.string("Nombre del ingrediente peruano"),
                                    "grams" to Schema.integer("Peso exacto en gramos"),
                                    "preparationNote" to Schema.string("Cómo cortarlo o cocinarlo")
                                )
                            )
                        ),
                        "steps" to Schema.array(Schema.string("Paso de preparación detallado"))
                    )
                )

                val config = generationConfig {
                    responseMimeType = "application/json"
                    responseSchema = recipeSchema
                    temperature = 0.7f
                }

                val model = Firebase.ai.generativeModel(
                    modelName = "gemini-3.8-flash",
                    generationConfig = config
                )

                val prompt = """
                    Eres un nutricionista deportivo peruano experto.
                    Crea una receta peruana deliciosa, saludable y exacta en gramos para un atleta cuyo objetivo es: ${state.userProfile.goal.label}.
                    Macros restantes que debe cubrir esta comida aproximadamente:
                    - Calorías objetivo: $remCalories kcal
                    - Proteína objetivo: ${remProtein.toInt()} g
                    - Carbohidratos objetivo: ${remCarbs.toInt()} g
                    - Grasas objetivo: ${remFat.toInt()} g
                    Prioriza usar estos ingredientes del mercado peruano: $ingredientsText.
                    Usa términos peruanos (palta, camote, quinua, papa, pescado, maní, choclo).
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    model.generateContent(prompt)
                }

                val jsonText = response.text ?: throw IllegalStateException("Respuesta vacía de Gemini")
                val obj = JSONObject(jsonText)

                val ingArray = obj.optJSONArray("ingredients")
                val parsedIngredients = mutableListOf<RecipeIngredient>()
                if (ingArray != null) {
                    for (i in 0 until ingArray.length()) {
                        val item = ingArray.getJSONObject(i)
                        parsedIngredients.add(
                            RecipeIngredient(
                                name = item.optString("name", "Ingrediente"),
                                grams = item.optInt("grams", 100),
                                preparationNote = item.optString("preparationNote", "")
                            )
                        )
                    }
                }

                val stepsArray = obj.optJSONArray("steps")
                val parsedSteps = mutableListOf<String>()
                if (stepsArray != null) {
                    for (i in 0 until stepsArray.length()) {
                        parsedSteps.add(stepsArray.getString(i))
                    }
                }

                val generatedMeal = SuggestedMeal(
                    id = "gemini_${UUID.randomUUID()}",
                    title = obj.optString("title", "Bowl Peruano Proteico con Quinua y Palta"),
                    description = obj.optString("description", "Receta generada con IA para tus macros restantes."),
                    prepTimeMinutes = obj.optInt("prepTimeMinutes", 20),
                    calories = obj.optInt("calories", remCalories),
                    proteinGrams = obj.optDouble("proteinGrams", remProtein.toDouble()).toFloat(),
                    carbsGrams = obj.optDouble("carbsGrams", remCarbs.toDouble()).toFloat(),
                    fatGrams = obj.optDouble("fatGrams", remFat.toDouble()).toFloat(),
                    mealType = runCatching { MealType.valueOf(obj.optString("mealType", "LUNCH")) }.getOrDefault(MealType.LUNCH),
                    reasonForGoal = obj.optString("reasonForGoal", "Ajustada a tus macros del día con ingredientes peruanos."),
                    ingredients = parsedIngredients.ifEmpty {
                        listOf(
                            RecipeIngredient("Filete de pescado o pollo", 180, "A la plancha"),
                            RecipeIngredient("Quinua o camote sancochado", 140, "Cocido"),
                            RecipeIngredient("Palta fuerte", 60, "En láminas")
                        )
                    },
                    steps = parsedSteps.ifEmpty {
                        listOf(
                            "Sazona la proteína con limón, ajo y sal de Maras y cocínala a la plancha.",
                            "Sirve acompañada de quinua graneada o camote sancochado y palta fresca en láminas."
                        )
                    }
                )

                _geminiState.value = GeminiRecipeState(
                    recipes = listOf(generatedMeal) + _geminiState.value.recipes,
                    isGenerating = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _geminiState.value = _geminiState.value.copy(
                    isGenerating = false,
                    errorMessage = "Se usó el generador nutricional local (detalle IA: ${e.localizedMessage ?: "sin conexión"})."
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
            syncCurrentStateToUserSheet()
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

    // Spotify & In-App Music Player controls
    fun connectSpotifyAccount() {
        spotifyConnector.connectSpotifyAccount(uiState.value.userProfile.email)
    }

    fun disconnectSpotifyAccount() {
        _isMusicPlaying.value = false
        musicJob?.cancel()
        spotifyConnector.disconnectSpotifyAccount()
    }

    fun handleSpotifyRedirectIntent(intent: Intent?) {
        spotifyConnector.handleAuthRedirectIntent(intent)
    }

    fun selectSpotifyPlaylist(playlist: SpotifyFitnessPlaylist) {
        val firstTrack = playlist.tracks.firstOrNull() ?: return
        selectMusicTrack(firstTrack)
    }

    fun openPlaylistInSpotifyApp(playlist: SpotifyFitnessPlaylist) {
        // Plays directly inside the in-app embedded Spotify player without leaving the app
        selectSpotifyPlaylist(playlist)
    }

    fun playLocalDeviceAudio(uri: Uri, title: String) {
        val customTrack = MusicTrack(
            id = "local_${System.currentTimeMillis()}",
            title = title,
            artist = "Mi Música Local",
            bpm = 128,
            genre = "Audio del Dispositivo",
            durationSeconds = 210,
            audioStreamUrl = uri.toString(),
            youtubeUrl = "",
            spotifyTrackUri = uri.toString(),
            playlistId = "pl_beast_mode"
        )
        _currentMusicTrack.value = customTrack
        _isMusicPlaying.value = true
        _musicProgress.value = 0f
        spotifyConnector.playUserDeviceAudioUri(uri, title)
        startMusicProgressLoop()
    }

    fun toggleMusicPlay() {
        val nextState = !_isMusicPlaying.value
        _isMusicPlaying.value = nextState
        if (nextState) {
            val track = _currentMusicTrack.value
            val playlist = MusicLibrary.getPlaylistById(track.playlistId)
            spotifyConnector.playPlaylistOrTrack(playlist, track)
            startMusicProgressLoop()
        } else {
            musicJob?.cancel()
            spotifyConnector.pausePlayback()
        }
    }

    fun selectMusicTrack(track: MusicTrack) {
        _currentMusicTrack.value = track
        _musicProgress.value = 0f
        _isMusicPlaying.value = true
        val playlist = MusicLibrary.getPlaylistById(track.playlistId)
        spotifyConnector.playPlaylistOrTrack(playlist, track)
        startMusicProgressLoop()
    }

    fun nextMusicTrack() {
        val tracks = MusicLibrary.tracks
        val currentIndex = tracks.indexOfFirst { it.id == _currentMusicTrack.value.id }
        val nextIndex = (currentIndex + 1) % tracks.size
        selectMusicTrack(tracks[nextIndex])
    }

    fun previousMusicTrack() {
        val tracks = MusicLibrary.tracks
        val currentIndex = tracks.indexOfFirst { it.id == _currentMusicTrack.value.id }
        val prevIndex = if (currentIndex <= 0) tracks.size - 1 else currentIndex - 1
        selectMusicTrack(tracks[prevIndex])
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

    override fun onCleared() {
        super.onCleared()
        spotifyConnector.stopAudioEngine()
    }

    // Meals actions
    fun addMeal(meal: MealItem) {
        viewModelScope.launch {
            repository.addMeal(meal)
            syncCurrentStateToUserSheet()
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            repository.deleteMeal(mealId)
            syncCurrentStateToUserSheet()
        }
    }

    // Measurement actions
    fun addMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch {
            repository.addMeasurement(measurement)
            val currentProfile = uiState.value.userProfile
            val updatedProfile = currentProfile.copy(currentWeightKg = measurement.weightKg)
            repository.saveUserProfile(updatedProfile)
            syncCurrentStateToUserSheet(overrideProfile = updatedProfile)
        }
    }

    fun deleteMeasurement(id: String) {
        viewModelScope.launch {
            repository.deleteMeasurement(id)
            syncCurrentStateToUserSheet()
        }
    }

    // Photo actions
    fun addPhoto(photo: ProgressPhoto) {
        viewModelScope.launch {
            repository.addPhoto(photo)
            syncCurrentStateToUserSheet()
        }
    }

    fun deletePhoto(id: String) {
        viewModelScope.launch {
            repository.deletePhoto(id)
            syncCurrentStateToUserSheet()
        }
    }

    // Account & 4-Hour Guest Session
    fun loginWithEmail(email: String) {
        requestEmailVerification(email, "1234")
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
