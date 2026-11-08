package com.example.data

import android.content.Context
import com.example.data.local.BodyMeasurementEntity
import com.example.data.local.CompletedExerciseEntity
import com.example.data.local.FitnessDatabase
import com.example.data.local.MealEntity
import com.example.data.local.ProgressPhotoEntity
import com.example.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitnessRepository(context: Context) {

    private val db = FitnessDatabase.getDatabase(context)
    private val dao = db.fitnessDao()

    // 4 hours in milliseconds = 4 * 60 * 60 * 1000 = 14,400,000 ms
    companion object {
        const val GUEST_SESSION_MAX_DURATION_MS = 4L * 60 * 60 * 1000
    }

    // USER PROFILE FLOW with 4-hour expiration check
    val userProfileFlow: Flow<UserProfile> = dao.getUserProfile().map { entity ->
        if (entity != null) {
            // Check if this was a guest session and if it expired (4 hours)
            if (entity.isGuestSession && entity.guestSessionStartedAt != null) {
                val elapsed = System.currentTimeMillis() - entity.guestSessionStartedAt
                if (elapsed > GUEST_SESSION_MAX_DURATION_MS) {
                    // 4 hours expired! Wipe all data and reset to fresh unauthenticated state
                    wipeAllUserData()
                    return@map getDefaultProfile()
                }
            }

            UserProfile(
                email = entity.email,
                isGuest = entity.isGuestSession,
                guestSessionStartTime = entity.guestSessionStartedAt,
                name = entity.name,
                age = entity.age,
                heightCm = entity.heightCm,
                currentWeightKg = entity.currentWeightKg,
                targetWeightKg = entity.targetWeightKg,
                gender = runCatching { Gender.valueOf(entity.gender) }.getOrDefault(Gender.MALE),
                activityLevel = runCatching { ActivityLevel.valueOf(entity.activityLevel) }.getOrDefault(ActivityLevel.MODERATE),
                goal = runCatching { FitnessGoal.valueOf(entity.goal) }.getOrDefault(FitnessGoal.LOSE_WEIGHT)
            )
        } else {
            getDefaultProfile()
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val entity = UserProfileEntity(
            id = 1,
            email = profile.email,
            isGuestSession = profile.isGuest,
            guestSessionStartedAt = profile.guestSessionStartTime,
            name = profile.name,
            age = profile.age,
            heightCm = profile.heightCm,
            currentWeightKg = profile.currentWeightKg,
            targetWeightKg = profile.targetWeightKg,
            gender = profile.gender.name,
            activityLevel = profile.activityLevel.name,
            goal = profile.goal.name
        )
        dao.saveUserProfile(entity)
    }

    // Login with Email (Permanent account)
    suspend fun loginWithEmail(email: String) {
        val current = userProfileFlow.firstOrNull() ?: getDefaultProfile()
        saveUserProfile(
            current.copy(
                email = email.trim(),
                isGuest = false,
                guestSessionStartTime = null
            )
        )
    }

    // Start 4-Hour Guest Session
    suspend fun startGuestSession() {
        val current = userProfileFlow.firstOrNull() ?: getDefaultProfile()
        saveUserProfile(
            current.copy(
                email = null,
                isGuest = true,
                guestSessionStartTime = System.currentTimeMillis()
            )
        )
    }

    // Logout and Wipe data
    suspend fun wipeAllUserData() {
        dao.clearUserProfile()
        dao.clearMeasurements()
        dao.clearMeals()
        dao.clearPhotos()
        dao.clearCompletedExercises()
    }

    // MEALS FLOW (Room)
    val mealsFlow: Flow<List<MealItem>> = dao.getAllMeals().map { list ->
        if (list.isEmpty()) {
            getInitialMeals()
        } else {
            list.map { entity ->
                MealItem(
                    id = entity.id,
                    name = entity.name,
                    mealType = runCatching { MealType.valueOf(entity.mealType) }.getOrDefault(MealType.LUNCH),
                    calories = entity.calories,
                    proteinGrams = entity.proteinGrams,
                    carbsGrams = entity.carbsGrams,
                    fatGrams = entity.fatGrams,
                    dateString = entity.dateString,
                    portionInfo = entity.portionInfo
                )
            }
        }
    }

    suspend fun addMeal(meal: MealItem) {
        dao.insertMeal(
            MealEntity(
                id = meal.id,
                name = meal.name,
                mealType = meal.mealType.name,
                calories = meal.calories,
                proteinGrams = meal.proteinGrams,
                carbsGrams = meal.carbsGrams,
                fatGrams = meal.fatGrams,
                dateString = meal.dateString,
                portionInfo = meal.portionInfo
            )
        )
    }

    suspend fun deleteMeal(mealId: String) {
        dao.deleteMeal(mealId)
    }

    // MEASUREMENTS FLOW (Room)
    val measurementsFlow: Flow<List<BodyMeasurement>> = dao.getAllMeasurements().map { list ->
        if (list.isEmpty()) {
            getInitialMeasurements()
        } else {
            list.map { entity ->
                BodyMeasurement(
                    id = entity.id,
                    dateEpochMillis = entity.dateEpochMillis,
                    dateFormatted = entity.dateFormatted,
                    weightKg = entity.weightKg,
                    chestCm = entity.chestCm,
                    waistCm = entity.waistCm,
                    hipsCm = entity.hipsCm,
                    armsCm = entity.armsCm,
                    thighCm = entity.thighCm,
                    notes = entity.notes
                )
            }
        }
    }

    suspend fun addMeasurement(measurement: BodyMeasurement) {
        dao.insertMeasurement(
            BodyMeasurementEntity(
                id = measurement.id,
                dateEpochMillis = measurement.dateEpochMillis,
                dateFormatted = measurement.dateFormatted,
                weightKg = measurement.weightKg,
                chestCm = measurement.chestCm,
                waistCm = measurement.waistCm,
                hipsCm = measurement.hipsCm,
                armsCm = measurement.armsCm,
                thighCm = measurement.thighCm,
                notes = measurement.notes
            )
        )
    }

    suspend fun deleteMeasurement(id: String) {
        dao.deleteMeasurement(id)
    }

    // PROGRESS PHOTOS FLOW (Room)
    val photosFlow: Flow<List<ProgressPhoto>> = dao.getAllPhotos().map { list ->
        list.map { entity ->
            ProgressPhoto(
                id = entity.id,
                dateEpochMillis = entity.dateEpochMillis,
                dateFormatted = entity.dateFormatted,
                photoUriString = entity.photoUriString,
                weightAtPhotoKg = entity.weightAtPhotoKg,
                label = entity.label,
                note = entity.note
            )
        }
    }

    suspend fun addPhoto(photo: ProgressPhoto) {
        dao.insertPhoto(
            ProgressPhotoEntity(
                id = photo.id,
                dateEpochMillis = photo.dateEpochMillis,
                dateFormatted = photo.dateFormatted,
                photoUriString = photo.photoUriString,
                weightAtPhotoKg = photo.weightAtPhotoKg,
                label = photo.label,
                note = photo.note
            )
        )
    }

    suspend fun deletePhoto(photoId: String) {
        dao.deletePhoto(photoId)
    }

    // COMPLETED EXERCISES FLOW (Room)
    val completedExercisesFlow: Flow<Set<String>> = dao.getCompletedExerciseIds().map { it.toSet() }

    suspend fun toggleExerciseCompleted(exerciseId: String) {
        val currentSet = dao.getCompletedExerciseIds().map { it.toSet() }.firstOrNull() ?: emptySet()
        if (currentSet.contains(exerciseId)) {
            dao.deleteCompletedExercise(exerciseId)
        } else {
            dao.insertCompletedExercise(CompletedExerciseEntity(exerciseId))
        }
    }

    private fun getDefaultProfile(): UserProfile {
        return UserProfile(
            email = null,
            isGuest = true,
            guestSessionStartTime = System.currentTimeMillis(),
            name = "Usuario",
            age = 26,
            heightCm = 175f,
            currentWeightKg = 76f,
            targetWeightKg = 70f,
            gender = Gender.MALE,
            activityLevel = ActivityLevel.MODERATE,
            goal = FitnessGoal.LOSE_WEIGHT
        )
    }

    private fun getInitialMeasurements(): List<BodyMeasurement> {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        val now = System.currentTimeMillis()
        val oneWeek = 7L * 24 * 60 * 60 * 1000
        return listOf(
            BodyMeasurement(
                dateEpochMillis = now - (oneWeek * 3),
                dateFormatted = sdf.format(Date(now - (oneWeek * 3))),
                weightKg = 78.4f,
                chestCm = 101f,
                waistCm = 86.5f,
                hipsCm = 99f,
                armsCm = 36.2f,
                notes = "Menos hinchazón"
            ),
            BodyMeasurement(
                dateEpochMillis = now - (oneWeek * 2),
                dateFormatted = sdf.format(Date(now - (oneWeek * 2))),
                weightKg = 77.5f,
                chestCm = 102f,
                waistCm = 85f,
                hipsCm = 98.5f,
                armsCm = 36.5f,
                notes = "Fuerza en aumento"
            ),
            BodyMeasurement(
                dateEpochMillis = now - oneWeek,
                dateFormatted = sdf.format(Date(now - oneWeek)),
                weightKg = 76.7f,
                chestCm = 102.5f,
                waistCm = 84f,
                hipsCm = 98f,
                armsCm = 36.8f,
                notes = "Buena adherencia a la dieta"
            ),
            BodyMeasurement(
                dateEpochMillis = now,
                dateFormatted = sdf.format(Date(now)),
                weightKg = 76.0f,
                chestCm = 103f,
                waistCm = 83f,
                hipsCm = 97.5f,
                armsCm = 37.0f,
                notes = "Progreso constante"
            )
        )
    }

    private fun getInitialMeals(): List<MealItem> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return listOf(
            MealItem(
                name = "Avena con leche y proteína",
                mealType = MealType.BREAKFAST,
                calories = 420,
                proteinGrams = 32f,
                carbsGrams = 54f,
                fatGrams = 8f,
                dateString = today,
                portionInfo = "1 bol grande"
            ),
            MealItem(
                name = "Pechuga de pollo a la plancha con arroz",
                mealType = MealType.LUNCH,
                calories = 540,
                proteinGrams = 48f,
                carbsGrams = 62f,
                fatGrams = 9f,
                dateString = today,
                portionInfo = "200g pollo + 150g arroz"
            ),
            MealItem(
                name = "Yogur griego con nueces y miel",
                mealType = MealType.SNACK,
                calories = 230,
                proteinGrams = 17f,
                carbsGrams = 18f,
                fatGrams = 10f,
                dateString = today,
                portionInfo = "1 vaso"
            )
        )
    }
}
