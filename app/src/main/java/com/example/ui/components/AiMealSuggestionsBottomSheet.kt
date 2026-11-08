package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FitnessGoal
import com.example.data.MealItem
import com.example.data.MealType
import com.example.data.UserProfile
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange

data class SuggestedMeal(
    val title: String,
    val description: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val mealType: MealType,
    val reasonForGoal: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMealSuggestionsBottomSheet(
    userProfile: UserProfile,
    remainingCalories: Int,
    remainingProtein: Float,
    remainingCarbs: Float,
    remainingFat: Float,
    onAcceptMeal: (MealItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isGenerating by remember { mutableStateOf(false) }

    // Generates smart dynamic recommendations tailored to the user's exact remaining macros and specific goal
    val suggestions = remember(remainingCalories, userProfile.goal) {
        generateSmartSuggestions(userProfile, remainingCalories, remainingProtein, remainingCarbs, remainingFat)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = NeonLime,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sugerencias Inteligentes IA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Macro summary header card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Tus Macros Restantes para Hoy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MacroBadge(name = "Calorías", value = "${remainingCalories.coerceAtLeast(0)} kcal", color = NeonLime)
                        MacroBadge(name = "Proteína", value = "${remainingProtein.toInt().coerceAtLeast(0)}g", color = AccentCyan)
                        MacroBadge(name = "Carbos", value = "${remainingCarbs.toInt().coerceAtLeast(0)}g", color = PrimaryOrange)
                        MacroBadge(name = "Grasas", value = "${remainingFat.toInt().coerceAtLeast(0)}g", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Opciones Optimizadas para ${userProfile.goal.label}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestions) { item ->
                    SuggestedMealCard(
                        meal = item,
                        onAdd = {
                            onAcceptMeal(
                                MealItem(
                                    name = item.title,
                                    mealType = item.mealType,
                                    calories = item.calories,
                                    proteinGrams = item.proteinGrams,
                                    carbsGrams = item.carbsGrams,
                                    fatGrams = item.fatGrams,
                                    portionInfo = item.description
                                )
                            )
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroBadge(name: String, value: String, color: Color) {
    Column {
        Text(text = name, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun SuggestedMealCard(
    meal: SuggestedMeal,
    onAdd: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonLime.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${meal.calories} kcal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonLime,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = meal.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Reason badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(
                        text = "✨ Por qué encaja: ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                    Text(
                        text = meal.reasonForGoal,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "P: ${meal.proteinGrams.toInt()}g", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                    Text(text = "C: ${meal.carbsGrams.toInt()}g", fontSize = 11.sp, color = NeonLime, fontWeight = FontWeight.Bold)
                    Text(text = "G: ${meal.fatGrams.toInt()}g", fontSize = 11.sp, color = PrimaryOrange, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir a Hoy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun generateSmartSuggestions(
    profile: UserProfile,
    cals: Int,
    protein: Float,
    carbs: Float,
    fat: Float
): List<SuggestedMeal> {
    return if (profile.goal == FitnessGoal.LOSE_WEIGHT) {
        listOf(
            SuggestedMeal(
                title = "Salmón o Atún con Ensalada de Espinacas y Quinoa",
                description = "160g de pescado azul + 80g de quinoa cocida con espinacas y limón",
                calories = (cals * 0.45f).toInt().coerceIn(320, 480),
                proteinGrams = (protein * 0.6f).coerceIn(35f, 48f),
                carbsGrams = (carbs * 0.35f).coerceIn(20f, 40f),
                fatGrams = 12f,
                mealType = MealType.DINNER,
                reasonForGoal = "Alta saciedad con proteína magra y ácidos grasos omega 3 que estimulan la quema de grasa sin elevar la insulina."
            ),
            SuggestedMeal(
                title = "Tortilla de Claras y Pavo con Espárragos a la plancha",
                description = "4 claras + 1 huevo entero + 80g pechuga de pavo braseada",
                calories = (cals * 0.35f).toInt().coerceIn(250, 360),
                proteinGrams = (protein * 0.5f).coerceIn(32f, 42f),
                carbsGrams = 6f,
                fatGrams = 7f,
                mealType = MealType.DINNER,
                reasonForGoal = "Bajo aporte calórico y máximo ratio proteico para proteger la masa muscular en déficit."
            ),
            SuggestedMeal(
                title = "Bol Proteico de Yogur Griego 0% y Semillas de Chía",
                description = "220g yogur griego + 15g chía + frutos rojos",
                calories = (cals * 0.25f).toInt().coerceIn(180, 260),
                proteinGrams = 24f,
                carbsGrams = 18f,
                fatGrams = 5f,
                mealType = MealType.SNACK,
                reasonForGoal = "Fibra soluble y caseína de digestión lenta para evitar antojos nocturnos."
            )
        )
    } else {
        // Gain Muscle / Hypertrophy
        listOf(
            SuggestedMeal(
                title = "Ternera Magra con Arroz Jazmín y Boniato Asado",
                description = "200g filete de ternera + 200g arroz jazmín + 100g boniato",
                calories = (cals * 0.5f).toInt().coerceIn(520, 720),
                proteinGrams = (protein * 0.55f).coerceIn(45f, 58f),
                carbsGrams = (carbs * 0.6f).coerceIn(65f, 90f),
                fatGrams = 14f,
                mealType = MealType.LUNCH,
                reasonForGoal = "Carbohidratos complejos densos para recarga de glucógeno y leucina para activar mTOR y síntesis de masa."
            ),
            SuggestedMeal(
                title = "Batido Anabólico de Avena, Plátano, Mantequilla de Cacahuete y Whey",
                description = "1 scoop de proteína + 70g avena molida + 1 plátano + 25g crema de cacahuete",
                calories = (cals * 0.4f).toInt().coerceIn(450, 600),
                proteinGrams = 38f,
                carbsGrams = 60f,
                fatGrams = 16f,
                mealType = MealType.SNACK,
                reasonForGoal = "Rápida absorción post-entreno con superávit calórico limpio y fácil de digerir."
            ),
            SuggestedMeal(
                title = "Pasta Integral con Pollo Desmenuzado y Salsa de Tomate Casera",
                description = "120g pasta integral en seco + 180g pechuga en dados + AOVE",
                calories = (cals * 0.45f).toInt().coerceIn(480, 650),
                proteinGrams = 46f,
                carbsGrams = 72f,
                fatGrams = 11f,
                mealType = MealType.DINNER,
                reasonForGoal = "Recarga energética sostenida durante el sueño para la reparación de tejido muscular."
            )
        )
    }
}
