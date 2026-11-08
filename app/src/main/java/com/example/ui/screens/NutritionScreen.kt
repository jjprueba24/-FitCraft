package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MealItem
import com.example.data.UserProfile
import com.example.ui.components.CalorieGoalDashboardCard
import com.example.ui.components.MealCardItem
import com.example.ui.theme.NeonLime

@Composable
fun NutritionScreen(
    userProfile: UserProfile,
    meals: List<MealItem>,
    onOpenAddMeal: () -> Unit,
    onOpenAiSuggestions: () -> Unit,
    onDeleteMeal: (String) -> Unit,
    onOpenProfileSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCalories = meals.sumOf { it.calories }
    val totalProtein = meals.sumOf { it.proteinGrams.toDouble() }.toFloat()
    val totalCarbs = meals.sumOf { it.carbsGrams.toDouble() }.toFloat()
    val totalFat = meals.sumOf { it.fatGrams.toDouble() }.toFloat()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("nutrition_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Daily Calorie Dashboard Card (Calculated according to height, weight, goal)
        item {
            CalorieGoalDashboardCard(
                userProfile = userProfile,
                totalCaloriesConsumed = totalCalories,
                totalProteinConsumed = totalProtein,
                totalCarbsConsumed = totalCarbs,
                totalFatConsumed = totalFat
            )
        }

        // Action buttons row (Registrar Comida + Sugerencias IA)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenAddMeal,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("btn_open_add_meal"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Registrar Comida",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onOpenAiSuggestions,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("btn_open_ai_suggestions"),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = NeonLime
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonLime)
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = NeonLime, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sugerir con IA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NeonLime
                    )
                }
            }
        }

        // Section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comidas de Hoy (${meals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total: $totalCalories kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonLime,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (meals.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No has registrado comidas hoy",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Presiona el botón de arriba para registrar tu desayuno, almuerzo, merienda o cena.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(meals, key = { it.id }) { meal ->
                MealCardItem(
                    meal = meal,
                    onDelete = { onDeleteMeal(meal.id) }
                )
            }
        }
    }
}
