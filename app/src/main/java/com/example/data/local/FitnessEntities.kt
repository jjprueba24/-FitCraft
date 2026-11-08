package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val email: String? = null,
    val isGuestSession: Boolean = false,
    val guestSessionStartedAt: Long? = null,
    val name: String = "Atleta",
    val age: Int = 26,
    val heightCm: Float = 175f,
    val currentWeightKg: Float = 75f,
    val targetWeightKg: Float = 70f,
    val gender: String = "MALE",
    val activityLevel: String = "MODERATE",
    val goal: String = "LOSE_WEIGHT",
    val trainingDaysPerWeek: Int = 5,
    val equipment: String = "Gimnasio Completo"
)

@Entity(tableName = "measurements")
data class BodyMeasurementEntity(
    @PrimaryKey val id: String,
    val dateEpochMillis: Long,
    val dateFormatted: String,
    val weightKg: Float,
    val chestCm: Float? = null,
    val waistCm: Float? = null,
    val hipsCm: Float? = null,
    val armsCm: Float? = null,
    val thighCm: Float? = null,
    val notes: String = ""
)

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey val id: String,
    val name: String,
    val mealType: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val dateString: String,
    val portionInfo: String
)

@Entity(tableName = "photos")
data class ProgressPhotoEntity(
    @PrimaryKey val id: String,
    val dateEpochMillis: Long,
    val dateFormatted: String,
    val photoUriString: String,
    val weightAtPhotoKg: Float,
    val label: String,
    val note: String
)

@Entity(tableName = "completed_exercises")
data class CompletedExerciseEntity(
    @PrimaryKey val exerciseId: String,
    val completedAt: Long = System.currentTimeMillis()
)
