package com.example

import com.example.data.ActivityLevel
import com.example.data.FitnessGoal
import com.example.data.Gender
import com.example.data.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileTest {

    private val base = UserProfile(
        age = 30,
        heightCm = 180f,
        currentWeightKg = 80f,
        gender = Gender.MALE,
        activityLevel = ActivityLevel.MODERATE,
        goal = FitnessGoal.MAINTAIN
    )

    @Test
    fun bmr_hombre_usaMifflinStJeor() {
        // 10*80 + 6.25*180 - 5*30 + 5 = 1780
        assertEquals(1780, base.bmr)
    }

    @Test
    fun bmr_mujer_restaOffsetFemenino() {
        val mujer = base.copy(gender = Gender.FEMALE)
        // 10*80 + 6.25*180 - 5*30 - 161 = 1614
        assertEquals(1614, mujer.bmr)
    }

    @Test
    fun tdee_aplicaMultiplicadorDeActividad() {
        assertEquals((1780 * 1.55f).toInt(), base.tdee)
    }

    @Test
    fun calorias_adelgazar_restaDeficit() {
        val perder = base.copy(goal = FitnessGoal.LOSE_WEIGHT)
        assertEquals(base.tdee - 450, perder.targetCalories)
    }

    @Test
    fun calorias_adelgazar_nuncaBajaDelMinimo() {
        val perfilBajo = UserProfile(
            age = 70,
            heightCm = 140f,
            currentWeightKg = 40f,
            gender = Gender.FEMALE,
            activityLevel = ActivityLevel.SEDENTARY,
            goal = FitnessGoal.LOSE_WEIGHT
        )
        assertEquals(1200, perfilBajo.targetCalories)
    }

    @Test
    fun calorias_ganarMusculo_sumaSuperavit() {
        val ganar = base.copy(goal = FitnessGoal.GAIN_MUSCLE)
        assertEquals(base.tdee + 350, ganar.targetCalories)
    }

    @Test
    fun proteina_dependeDeLaMeta() {
        assertEquals(176, base.copy(goal = FitnessGoal.GAIN_MUSCLE).targetProteinGrams)
        assertEquals(160, base.copy(goal = FitnessGoal.LOSE_WEIGHT).targetProteinGrams)
        assertEquals(128, base.copy(goal = FitnessGoal.MAINTAIN).targetProteinGrams)
    }

    @Test
    fun imc_secalculaConEstaturaEnMetros() {
        assertEquals(24.69f, base.bmi, 0.01f)
    }

    @Test
    fun carbohidratos_nuncaBajanDe50() {
        assertTrue(base.targetCarbsGrams >= 50)
    }
}
