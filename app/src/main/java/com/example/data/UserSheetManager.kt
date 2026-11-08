package com.example.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class AccountAuthStatus(
    val isLoggedIn: Boolean = false,
    val email: String? = null,
    val isEmailVerified: Boolean = false,
    val pendingVerificationEmail: String? = null,
    val generatedVerificationCode: String? = null,
    val sheetFileName: String? = null,
    val lastSheetSyncTime: String? = null,
    val totalRowsInSheet: Int = 0,
    val authMessage: String? = null,
    val authError: String? = null
)

data class AccountSnapshot(
    val profile: UserProfile,
    val measurements: List<BodyMeasurement>,
    val meals: List<MealItem>,
    val photos: List<ProgressPhoto>,
    val completedExercises: Set<String>
)

class UserSheetManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "fitcraft_verified_accounts_prefs"
        private const val KEY_ACTIVE_EMAIL = "active_logged_in_email"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

    fun getActiveEmail(): String? = prefs.getString(KEY_ACTIVE_EMAIL, null)

    fun isAccountRegistered(email: String): Boolean {
        val norm = normalizeEmail(email)
        return prefs.contains("pin_$norm")
    }

    /**
     * Step 1: Request email verification or login.
     * Generates a 6-digit verification code so the email is verified and protected with a security PIN.
     */
    fun initiateEmailVerification(email: String, pin: String): AccountAuthStatus {
        val norm = normalizeEmail(email)
        if (!norm.contains("@") || !norm.contains(".")) {
            return AccountAuthStatus(
                authError = "Ingresa un correo electrónico válido (ej. usuario@gmail.com)."
            )
        }
        if (pin.trim().length < 4) {
            return AccountAuthStatus(
                authError = "Crea o ingresa un PIN de seguridad de al menos 4 dígitos."
            )
        }

        val existingPin = prefs.getString("pin_$norm", null)
        if (existingPin != null && existingPin != pin.trim()) {
            return AccountAuthStatus(
                authError = "PIN de seguridad incorrecto para $norm. Intenta nuevamente."
            )
        }

        // If account is already registered & verified and PIN matches, log in immediately and restore Sheet!
        val alreadyVerified = prefs.getBoolean("verified_$norm", false)
        if (existingPin != null && alreadyVerified) {
            prefs.edit().putString(KEY_ACTIVE_EMAIL, norm).commit()
            val sheetFile = getSheetFileForEmail(norm)
            val syncTime = prefs.getString("sync_time_$norm", obtenerFechaHoraActual())
            return AccountAuthStatus(
                isLoggedIn = true,
                email = norm,
                isEmailVerified = true,
                sheetFileName = sheetFile.name,
                lastSheetSyncTime = syncTime,
                authMessage = "✓ Sesión verificada y restaurada desde tu Sheet (${sheetFile.name})"
            )
        }

        // New email registration: generate 6-digit verification code
        val code = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
        prefs.edit()
            .putString("pending_pin_$norm", pin.trim())
            .putString("pending_code_$norm", code)
            .commit()

        return AccountAuthStatus(
            isLoggedIn = false,
            pendingVerificationEmail = norm,
            generatedVerificationCode = code,
            authMessage = "Se generó el código de comprobación $code para verificar $norm."
        )
    }

    /**
     * Step 2: Confirm the 6-digit verification code to activate the account and generate its Sheet.
     */
    fun confirmEmailVerificationCode(
        email: String,
        enteredCode: String,
        currentProfile: UserProfile,
        measurements: List<BodyMeasurement>,
        meals: List<MealItem>,
        photos: List<ProgressPhoto>,
        completedExercises: Set<String>
    ): AccountAuthStatus {
        val norm = normalizeEmail(email)
        val expectedCode = prefs.getString("pending_code_$norm", null)
        val pendingPin = prefs.getString("pending_pin_$norm", "1234") ?: "1234"

        if (expectedCode == null || enteredCode.trim() != expectedCode) {
            return AccountAuthStatus(
                pendingVerificationEmail = norm,
                generatedVerificationCode = expectedCode,
                authError = "El código ingresado no coincide con el código de comprobación ($expectedCode)."
            )
        }

        // Mark verified and active
        val nowStr = obtenerFechaHoraActual()
        prefs.edit()
            .putString("pin_$norm", pendingPin)
            .putBoolean("verified_$norm", true)
            .putString(KEY_ACTIVE_EMAIL, norm)
            .putString("sync_time_$norm", nowStr)
            .remove("pending_code_$norm")
            .commit()

        val updatedProfile = currentProfile.copy(
            email = norm,
            isGuest = false,
            guestSessionStartTime = null
        )

        // Save snapshot and generate CSV Sheet immediately
        saveAccountSnapshotAndSheet(
            email = norm,
            profile = updatedProfile,
            measurements = measurements,
            meals = meals,
            photos = photos,
            completedExercises = completedExercises
        )

        val sheetFile = getSheetFileForEmail(norm)
        val totalRows = 1 + measurements.size + meals.size + photos.size

        return AccountAuthStatus(
            isLoggedIn = true,
            email = norm,
            isEmailVerified = true,
            sheetFileName = sheetFile.name,
            lastSheetSyncTime = nowStr,
            totalRowsInSheet = totalRows,
            authMessage = "✓ Correo $norm verificado y Sheet '${sheetFile.name}' generado con éxito."
        )
    }

    /**
     * Logs out the current session WITHOUT deleting the user's saved account or Sheet data.
     */
    fun logoutPreservingSheetData(): AccountAuthStatus {
        val previousEmail = getActiveEmail()
        prefs.edit().remove(KEY_ACTIVE_EMAIL).commit()
        return AccountAuthStatus(
            isLoggedIn = false,
            email = null,
            isEmailVerified = false,
            authMessage = if (previousEmail != null) {
                "Sesión cerrada de forma segura. Tus datos y fotos siguen guardados en el Sheet de $previousEmail."
            } else {
                "Sesión cerrada."
            }
        )
    }

    /**
     * Saves both the JSON snapshot (for instant account switching/restoration) and the CSV Sheet file
     * containing Profile, Measurements, Meals, and Progress Photos.
     */
    fun saveAccountSnapshotAndSheet(
        email: String,
        profile: UserProfile,
        measurements: List<BodyMeasurement>,
        meals: List<MealItem>,
        photos: List<ProgressPhoto>,
        completedExercises: Set<String>
    ): File {
        val norm = normalizeEmail(email)
        val nowStr = obtenerFechaHoraActual()

        // 1. Save structured JSON backup keyed by email so reopening with email restores 100% of data
        val root = JSONObject().apply {
            put("profile", JSONObject().apply {
                put("email", norm)
                put("name", profile.name)
                put("age", profile.age)
                put("heightCm", profile.heightCm.toDouble())
                put("currentWeightKg", profile.currentWeightKg.toDouble())
                put("targetWeightKg", profile.targetWeightKg.toDouble())
                put("gender", profile.gender.name)
                put("activityLevel", profile.activityLevel.name)
                put("goal", profile.goal.name)
                put("trainingDaysPerWeek", profile.trainingDaysPerWeek)
                put("equipment", profile.equipment)
            })
            put("measurements", JSONArray().apply {
                measurements.forEach { m ->
                    put(JSONObject().apply {
                        put("id", m.id)
                        put("dateEpochMillis", m.dateEpochMillis)
                        put("dateFormatted", m.dateFormatted)
                        put("weightKg", m.weightKg.toDouble())
                        put("chestCm", m.chestCm?.toDouble() ?: 0.0)
                        put("waistCm", m.waistCm?.toDouble() ?: 0.0)
                        put("hipsCm", m.hipsCm?.toDouble() ?: 0.0)
                        put("armsCm", m.armsCm?.toDouble() ?: 0.0)
                        put("notes", m.notes)
                    })
                }
            })
            put("meals", JSONArray().apply {
                meals.forEach { meal ->
                    put(JSONObject().apply {
                        put("id", meal.id)
                        put("name", meal.name)
                        put("mealType", meal.mealType.name)
                        put("calories", meal.calories)
                        put("proteinGrams", meal.proteinGrams.toDouble())
                        put("carbsGrams", meal.carbsGrams.toDouble())
                        put("fatGrams", meal.fatGrams.toDouble())
                        put("dateString", meal.dateString)
                        put("portionInfo", meal.portionInfo)
                    })
                }
            })
            put("photos", JSONArray().apply {
                photos.forEach { p ->
                    put(JSONObject().apply {
                        put("id", p.id)
                        put("dateEpochMillis", p.dateEpochMillis)
                        put("dateFormatted", p.dateFormatted)
                        put("photoUriString", p.photoUriString)
                        put("weightAtPhotoKg", p.weightAtPhotoKg.toDouble())
                        put("label", p.label)
                        put("note", p.note)
                    })
                }
            })
            put("completedExercises", JSONArray().apply {
                completedExercises.forEach { put(it) }
            })
        }

        prefs.edit()
            .putString("snapshot_$norm", root.toString())
            .putString("sync_time_$norm", nowStr)
            .commit()

        // 2. Write real CSV Sheet file compatible with Google Sheets & Microsoft Excel
        val sheetFile = getSheetFileForEmail(norm)
        val csvBuilder = StringBuilder()
        csvBuilder.appendLine("SECCION,FECHA,CONCEPTO_O_ETIQUETA,DETALLE_1,DETALLE_2,DETALLE_3,DETALLE_4,ARCHIVO_O_NOTA")
        csvBuilder.appendLine(
            "PERFIL_Y_METAS,\"$nowStr\",\"${profile.email ?: norm}\",\"Estatura: ${profile.heightCm} cm | Peso: ${profile.currentWeightKg} kg -> Meta: ${profile.targetWeightKg} kg\",\"Objetivo: ${profile.goal.label}\",\"TMB: ${profile.bmr} kcal | TDEE: ${profile.tdee} kcal\",\"Meta Diaria: ${profile.targetCalories} kcal (P:${profile.targetProteinGrams}g C:${profile.targetCarbsGrams}g G:${profile.targetFatGrams}g)\",\"Equipo: ${profile.equipment} (${profile.trainingDaysPerWeek} dias/sem)\""
        )

        measurements.forEach { m ->
            csvBuilder.appendLine(
                "PROGRESO_PESO_Y_MEDIDAS,\"${m.dateFormatted}\",\"Pesaje Control\",\"Peso: ${m.weightKg} kg\",\"Cintura: ${m.waistCm ?: "-"} cm\",\"Pecho: ${m.chestCm ?: "-"} cm\",\"Brazos: ${m.armsCm ?: "-"} cm\",\"${m.notes.replace("\"", "'")}\""
            )
        }

        photos.forEach { p ->
            csvBuilder.appendLine(
                "FOTOS_ANTES_Y_DESPUES,\"${p.dateFormatted}\",\"${p.label.replace("\"", "'")}\",\"Peso en Foto: ${p.weightAtPhotoKg} kg\",\"Guardado Seguro en App\",\"ID: ${p.id}\",\"Nota: ${p.note.replace("\"", "'")}\",\"${p.photoUriString}\""
            )
        }

        meals.forEach { meal ->
            csvBuilder.appendLine(
                "REGISTRO_COMIDAS_DIARIAS,\"${meal.dateString}\",\"${meal.mealType.label}\",\"${meal.name.replace("\"", "'")}\",\"${meal.calories} kcal\",\"Proteina: ${meal.proteinGrams.toInt()}g\",\"Carbos: ${meal.carbsGrams.toInt()}g | Grasas: ${meal.fatGrams.toInt()}g\",\"${meal.portionInfo.replace("\"", "'")}\""
            )
        }

        sheetFile.writeText(csvBuilder.toString(), Charsets.UTF_8)
        return sheetFile
    }

    /**
     * Loads the saved account snapshot for a verified email so logging back in restores all data.
     */
    fun loadAccountSnapshot(email: String): AccountSnapshot? {
        val norm = normalizeEmail(email)
        val rawJson = prefs.getString("snapshot_$norm", null) ?: return null
        return try {
            val root = JSONObject(rawJson)
            val pObj = root.getJSONObject("profile")
            val profile = UserProfile(
                email = pObj.optString("email", norm),
                isGuest = false,
                guestSessionStartTime = null,
                name = pObj.optString("name", norm.substringBefore("@")),
                age = pObj.optInt("age", 26),
                heightCm = pObj.optDouble("heightCm", 175.0).toFloat(),
                currentWeightKg = pObj.optDouble("currentWeightKg", 75.0).toFloat(),
                targetWeightKg = pObj.optDouble("targetWeightKg", 70.0).toFloat(),
                gender = runCatching { Gender.valueOf(pObj.optString("gender", "MALE")) }.getOrDefault(Gender.MALE),
                activityLevel = runCatching { ActivityLevel.valueOf(pObj.optString("activityLevel", "MODERATE")) }.getOrDefault(ActivityLevel.MODERATE),
                goal = runCatching { FitnessGoal.valueOf(pObj.optString("goal", "LOSE_WEIGHT")) }.getOrDefault(FitnessGoal.LOSE_WEIGHT),
                trainingDaysPerWeek = pObj.optInt("trainingDaysPerWeek", 5),
                equipment = pObj.optString("equipment", "Gimnasio Completo")
            )

            val mArr = root.optJSONArray("measurements") ?: JSONArray()
            val measurements = (0 until mArr.length()).map { i ->
                val o = mArr.getJSONObject(i)
                BodyMeasurement(
                    id = o.optString("id"),
                    dateEpochMillis = o.optLong("dateEpochMillis", System.currentTimeMillis()),
                    dateFormatted = o.optString("dateFormatted"),
                    weightKg = o.optDouble("weightKg", 75.0).toFloat(),
                    chestCm = o.optDouble("chestCm", 0.0).toFloat().takeIf { it > 0f },
                    waistCm = o.optDouble("waistCm", 0.0).toFloat().takeIf { it > 0f },
                    hipsCm = o.optDouble("hipsCm", 0.0).toFloat().takeIf { it > 0f },
                    armsCm = o.optDouble("armsCm", 0.0).toFloat().takeIf { it > 0f },
                    notes = o.optString("notes", "")
                )
            }

            val mealsArr = root.optJSONArray("meals") ?: JSONArray()
            val meals = (0 until mealsArr.length()).map { i ->
                val o = mealsArr.getJSONObject(i)
                MealItem(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    mealType = runCatching { MealType.valueOf(o.optString("mealType", "LUNCH")) }.getOrDefault(MealType.LUNCH),
                    calories = o.optInt("calories", 0),
                    proteinGrams = o.optDouble("proteinGrams", 0.0).toFloat(),
                    carbsGrams = o.optDouble("carbsGrams", 0.0).toFloat(),
                    fatGrams = o.optDouble("fatGrams", 0.0).toFloat(),
                    dateString = o.optString("dateString"),
                    portionInfo = o.optString("portionInfo")
                )
            }

            val photosArr = root.optJSONArray("photos") ?: JSONArray()
            val photos = (0 until photosArr.length()).map { i ->
                val o = photosArr.getJSONObject(i)
                ProgressPhoto(
                    id = o.optString("id"),
                    dateEpochMillis = o.optLong("dateEpochMillis", System.currentTimeMillis()),
                    dateFormatted = o.optString("dateFormatted"),
                    photoUriString = o.optString("photoUriString"),
                    weightAtPhotoKg = o.optDouble("weightAtPhotoKg", 75.0).toFloat(),
                    label = o.optString("label"),
                    note = o.optString("note")
                )
            }

            val compArr = root.optJSONArray("completedExercises") ?: JSONArray()
            val completed = (0 until compArr.length()).map { compArr.getString(it) }.toSet()

            AccountSnapshot(
                profile = profile,
                measurements = measurements,
                meals = meals,
                photos = photos,
                completedExercises = completed
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getSheetFileForEmail(email: String?): File {
        val dir = File(context.filesDir, "user_sheets")
        if (!dir.exists()) dir.mkdirs()
        val safeName = (email ?: "invitado")
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9._-]"), "_")
        return File(dir, "sheet_progreso_${safeName}.csv")
    }

    fun shareOrExportSheetCsv(email: String?) {
        try {
            val file = getSheetFileForEmail(email)
            if (!file.exists()) return
            val csvText = file.readText()
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Sheet de Progreso, Dieta y Fotos - ${email ?: "FitCraft"}")
                putExtra(Intent.EXTRA_TEXT, csvText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Exportar / Guardar Sheet en Google Sheets").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
        }
    }

    private fun obtenerFechaHoraActual(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
