package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    // PROFILE & AUTH
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()

    // MEASUREMENTS
    @Query("SELECT * FROM measurements ORDER BY dateEpochMillis ASC")
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: BodyMeasurementEntity)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteMeasurement(id: String)

    @Query("DELETE FROM measurements")
    suspend fun clearMeasurements()

    // MEALS
    @Query("SELECT * FROM meals ORDER BY rowid DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: String)

    @Query("DELETE FROM meals")
    suspend fun clearMeals()

    // PROGRESS PHOTOS
    @Query("SELECT * FROM photos ORDER BY dateEpochMillis ASC")
    fun getAllPhotos(): Flow<List<ProgressPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: ProgressPhotoEntity)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhoto(id: String)

    @Query("DELETE FROM photos")
    suspend fun clearPhotos()

    // COMPLETED EXERCISES
    @Query("SELECT exerciseId FROM completed_exercises")
    fun getCompletedExerciseIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedExercise(exercise: CompletedExerciseEntity)

    @Query("DELETE FROM completed_exercises WHERE exerciseId = :exerciseId")
    suspend fun deleteCompletedExercise(exerciseId: String)

    @Query("DELETE FROM completed_exercises")
    suspend fun clearCompletedExercises()

    // CLEAR ALL DATA (When 4-hour guest session expires or user logs out)
    @Query("DELETE FROM meals")
    suspend fun wipeAllMeals()
}
