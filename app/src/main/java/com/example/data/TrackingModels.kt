package com.example.data

enum class MealType(val label: String, val iconName: String) {
    BREAKFAST("Desayuno", "wb_sunny"),
    LUNCH("Almuerzo", "restaurant"),
    SNACK("Merienda / Snack", "local_cafe"),
    DINNER("Cena", "nights_stay")
}

data class MealItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val mealType: MealType,
    val calories: Int,
    val proteinGrams: Float = 0f,
    val carbsGrams: Float = 0f,
    val fatGrams: Float = 0f,
    val dateString: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
    val portionInfo: String = "1 ración"
)

data class BodyMeasurement(
    val id: String = java.util.UUID.randomUUID().toString(),
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val dateFormatted: String, // ej. "12 Oct"
    val weightKg: Float,
    val chestCm: Float? = null,
    val waistCm: Float? = null,
    val hipsCm: Float? = null,
    val armsCm: Float? = null,
    val thighCm: Float? = null,
    val notes: String = ""
)

data class ProgressPhoto(
    val id: String = java.util.UUID.randomUUID().toString(),
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val dateFormatted: String,
    val photoUriString: String,
    val weightAtPhotoKg: Float,
    val label: String = "Progreso", // ej: "Antes (Día 1)", "Semana 4", "Semana 8", "Hoy"
    val note: String = ""
)
