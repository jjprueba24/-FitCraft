package com.example.data

enum class FitnessGoal(val label: String, val description: String) {
    LOSE_WEIGHT("Adelgazar / Definir", "Déficit calórico moderado para quemar grasa preservando masa muscular"),
    GAIN_MUSCLE("Ganar Músculo / Hipertrofia", "Superávit calórico controlado para maximizar la síntesis de masa muscular"),
    MAINTAIN("Mantenimiento & Salud", "Equilibrio calórico para rendimiento y recomposición corporal gradual")
}

enum class ActivityLevel(val label: String, val multiplier: Float) {
    SEDENTARY("Sedentario (poco o ningún ejercicio)", 1.2f),
    LIGHT("Ligero (ejercicio 1-3 días/sem)", 1.375f),
    MODERATE("Moderado (ejercicio 3-5 días/sem)", 1.55f),
    VERY_ACTIVE("Muy Activo (ejercicio 6-7 días/sem)", 1.725f)
}

enum class Gender(val label: String) {
    MALE("Hombre"),
    FEMALE("Mujer")
}

data class UserProfile(
    val email: String? = null,
    val isGuest: Boolean = true,
    val guestSessionStartTime: Long? = System.currentTimeMillis(),
    val name: String = "Atleta",
    val age: Int = 26,
    val heightCm: Float = 175f,
    val currentWeightKg: Float = 75f,
    val targetWeightKg: Float = 70f,
    val gender: Gender = Gender.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goal: FitnessGoal = FitnessGoal.LOSE_WEIGHT,
    val trainingDaysPerWeek: Int = 5,
    val equipment: String = "Gimnasio Completo"
) {
    // Harris-Benedict revised equation (Mifflin-St Jeor)
    val bmr: Int
        get() {
            val base = (10f * currentWeightKg) + (6.25f * heightCm) - (5f * age)
            val genderOffset = if (gender == Gender.MALE) 5f else -161f
            return (base + genderOffset).toInt()
        }

    val tdee: Int
        get() = (bmr * activityLevel.multiplier).toInt()

    val targetCalories: Int
        get() = when (goal) {
            FitnessGoal.LOSE_WEIGHT -> (tdee - 450).coerceAtLeast(1200)
            FitnessGoal.GAIN_MUSCLE -> tdee + 350
            FitnessGoal.MAINTAIN -> tdee
        }

    // Recommended daily macronutrient targets
    val targetProteinGrams: Int
        get() = when (goal) {
            FitnessGoal.GAIN_MUSCLE -> (currentWeightKg * 2.2f).toInt()
            FitnessGoal.LOSE_WEIGHT -> (currentWeightKg * 2.0f).toInt()
            FitnessGoal.MAINTAIN -> (currentWeightKg * 1.6f).toInt()
        }

    val targetFatGrams: Int
        get() = ((targetCalories * 0.25f) / 9f).toInt()

    val targetCarbsGrams: Int
        get() {
            val remainingCals = targetCalories - (targetProteinGrams * 4) - (targetFatGrams * 9)
            return (remainingCals / 4f).coerceAtLeast(50f).toInt()
        }

    val bmi: Float
        get() {
            val hM = heightCm / 100f
            return if (hM > 0) currentWeightKg / (hM * hM) else 0f
        }

    val remainingGuestTimeMillis: Long
        get() {
            if (!isGuest || guestSessionStartTime == null) return Long.MAX_VALUE
            val elapsed = System.currentTimeMillis() - guestSessionStartTime
            val left = FitnessRepository.GUEST_SESSION_MAX_DURATION_MS - elapsed
            return left.coerceAtLeast(0L)
        }

    val isGuestSessionExpired: Boolean
        get() = isGuest && remainingGuestTimeMillis <= 0L
}
