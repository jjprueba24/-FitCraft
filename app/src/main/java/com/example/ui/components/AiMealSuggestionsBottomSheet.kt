package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange
import kotlin.math.roundToInt

data class RecipeIngredient(
    val name: String,
    val grams: Int,
    val preparationNote: String = ""
)

data class SuggestedMeal(
    val id: String,
    val title: String,
    val description: String,
    val prepTimeMinutes: Int,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val mealType: MealType,
    val reasonForGoal: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMealSuggestionsBottomSheet(
    userProfile: UserProfile,
    remainingCalories: Int,
    remainingProtein: Float,
    remainingCarbs: Float,
    remainingFat: Float,
    geminiGeneratedRecipes: List<SuggestedMeal> = emptyList(),
    isGeneratingGeminiRecipe: Boolean = false,
    geminiErrorMessage: String? = null,
    onGenerateWithGemini: (List<String>) -> Unit = {},
    onAcceptMeal: (MealItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategoryFilter by remember { mutableStateOf("Todas") }

    val peruvianMarketIngredients = listOf(
        "Palta", "Camote", "Quinua", "Pescado", "Pollo", "Lentejas", "Papa", "Huevos", "Maní", "Choclo"
    )
    val selectedIngredients = remember {
        mutableStateListOf("Palta", "Camote", "Quinua", "Pescado")
    }

    val presetRecipes = remember(remainingCalories, remainingProtein, remainingCarbs, remainingFat, userProfile.goal) {
        generateTailoredPeruvianRecipes(
            profile = userProfile,
            remainingCalories = remainingCalories,
            remainingProtein = remainingProtein,
            remainingCarbs = remainingCarbs,
            remainingFat = remainingFat
        )
    }

    val combinedRecipes = remember(geminiGeneratedRecipes, presetRecipes) {
        geminiGeneratedRecipes + presetRecipes
    }

    val filteredRecipes = remember(combinedRecipes, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "Almuerzo / Cena" -> combinedRecipes.filter { it.mealType == MealType.LUNCH || it.mealType == MealType.DINNER }
            "Desayuno / Post" -> combinedRecipes.filter { it.mealType == MealType.BREAKFAST || it.mealType == MealType.SNACK }
            "Rápidas (≤15m)" -> combinedRecipes.filter { it.prepTimeMinutes <= 15 }
            else -> combinedRecipes
        }
    }

    var expandedRecipeId by remember(filteredRecipes) {
        mutableStateOf(filteredRecipes.firstOrNull()?.id.orEmpty())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
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
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Chef Gemini IA • Recetas Peruanas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gemini 3.8 Flash + Ingredientes del mercado peruano",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Macro summary header card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MacroBadge(name = "Calorías Faltantes", value = "${remainingCalories.coerceAtLeast(0)} kcal", color = NeonLime)
                        MacroBadge(name = "Proteína", value = "${remainingProtein.toInt().coerceAtLeast(0)}g", color = AccentCyan)
                        MacroBadge(name = "Carbos", value = "${remainingCarbs.toInt().coerceAtLeast(0)}g", color = PrimaryOrange)
                        MacroBadge(name = "Grasas", value = "${remainingFat.toInt().coerceAtLeast(0)}g", color = MaterialTheme.colorScheme.secondary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Elige ingredientes peruanos para tu receta con Gemini IA:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(peruvianMarketIngredients.size) { i ->
                            val ing = peruvianMarketIngredients[i]
                            val isSelected = selectedIngredients.contains(ing)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NeonLime.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) NeonLime else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isSelected && selectedIngredients.size > 1) {
                                            selectedIngredients.remove(ing)
                                        } else if (!isSelected) {
                                            selectedIngredients.add(ing)
                                        }
                                    }
                            ) {
                                Text(
                                    text = if (isSelected) "✓ $ing" else ing,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonLime else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onGenerateWithGemini(selectedIngredients.toList()) },
                        enabled = !isGeneratingGeminiRecipe,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = Color(0xFF0A1202)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("btn_generate_gemini_recipe")
                    ) {
                        if (isGeneratingGeminiRecipe) {
                            CircularProgressIndicator(
                                color = Color(0xFF0A1202),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini está creando tu receta peruana...", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Generar Receta con Gemini IA (${selectedIngredients.joinToString(", ")})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }

                    if (!geminiErrorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = geminiErrorMessage,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentCoral
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter chips
            val filters = listOf("Todas", "Almuerzo / Cena", "Desayuno / Post", "Rápidas (≤15m)")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters.size) { index ->
                    val label = filters[index]
                    val isSelected = selectedCategoryFilter == label
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedCategoryFilter = label }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                itemsIndexed(filteredRecipes, key = { _, item -> item.id }) { _, recipe ->
                    val isExpanded = expandedRecipeId == recipe.id
                    TailoredRecipeCard(
                        recipe = recipe,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedRecipeId = if (isExpanded) "" else recipe.id
                        },
                        onAdd = {
                            onAcceptMeal(
                                MealItem(
                                    name = recipe.title,
                                    mealType = recipe.mealType,
                                    calories = recipe.calories,
                                    proteinGrams = recipe.proteinGrams,
                                    carbsGrams = recipe.carbsGrams,
                                    fatGrams = recipe.fatGrams,
                                    portionInfo = recipe.ingredients.joinToString(", ") { "${it.grams}g ${it.name}" }
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
private fun TailoredRecipeCard(
    recipe: SuggestedMeal,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAdd: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(
            1.dp,
            if (isExpanded) NeonLime.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onToggleExpand() }
            .testTag("recipe_card_${recipe.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentCyan.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = recipe.mealType.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${recipe.prepTimeMinutes} min",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = recipe.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NeonLime.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${recipe.calories} kcal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonLime,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Text(
                text = recipe.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingredientes Exactos (en gramos):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    recipe.ingredients.forEach { ing ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(AccentCyan, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (ing.preparationNote.isNotBlank()) {
                                        "${ing.name} (${ing.preparationNote})"
                                    } else {
                                        ing.name
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "${ing.grams} g",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonLime,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Preparación Paso a Paso:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    recipe.steps.forEachIndexed { idx, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryOrange.copy(alpha = 0.22f),
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryOrange
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Por qué encaja con tu meta: ${recipe.reasonForGoal}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "P: ${recipe.proteinGrams.toInt()}g", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                    Text(text = "C: ${recipe.carbsGrams.toInt()}g", fontSize = 11.sp, color = NeonLime, fontWeight = FontWeight.Bold)
                    Text(text = "G: ${recipe.fatGrams.toInt()}g", fontSize = 11.sp, color = PrimaryOrange, fontWeight = FontWeight.Bold)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onToggleExpand() }
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isExpanded) "Ocultar" else "Pasos",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_add_recipe_${recipe.id}")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir a Hoy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun scaleGrams(baseGrams: Int, factor: Float): Int {
    return ((baseGrams * factor) / 5f).roundToInt() * 5
}

private fun generateTailoredPeruvianRecipes(
    profile: UserProfile,
    remainingCalories: Int,
    remainingProtein: Float,
    remainingCarbs: Float,
    remainingFat: Float
): List<SuggestedMeal> {
    val targetMealCals = when {
        remainingCalories > 900 -> (remainingCalories * 0.55f).toInt().coerceIn(420, 750)
        remainingCalories in 320..900 -> remainingCalories
        else -> if (profile.goal == FitnessGoal.LOSE_WEIGHT) 360 else 520
    }

    return if (profile.goal == FitnessGoal.LOSE_WEIGHT) {
        val factor1 = (targetMealCals / 430f).coerceIn(0.75f, 1.45f)
        val factor2 = (targetMealCals / 390f).coerceIn(0.75f, 1.45f)
        val factor3 = (targetMealCals / 410f).coerceIn(0.75f, 1.45f)
        val factor4 = ((targetMealCals * 0.75f) / 310f).coerceIn(0.75f, 1.35f)

        listOf(
            SuggestedMeal(
                id = "rec_pollo_quinua_palta",
                title = "Pollo a la Plancha con Quinua Graneada y Palta Fuerte",
                description = "Plato peruano alto en proteína magra, fibra y grasas monoinsaturadas para máxima saciedad.",
                prepTimeMinutes = 18,
                calories = (430 * factor1).roundToInt(),
                proteinGrams = (48f * factor1).coerceAtLeast(32f),
                carbsGrams = (34f * factor1).coerceAtLeast(20f),
                fatGrams = (11f * factor1).coerceAtLeast(7f),
                mealType = MealType.LUNCH,
                reasonForGoal = "La quinua aporta aminoácidos completos de bajo índice glucémico y la palta controla el apetito por horas.",
                ingredients = listOf(
                    RecipeIngredient("Pechuga de pollo sin piel", scaleGrams(180, factor1), "en filete"),
                    RecipeIngredient("Quinua blanca o tricolor cocida", scaleGrams(130, factor1), "graneada sin aceite"),
                    RecipeIngredient("Palta fuerte", scaleGrams(55, factor1), "en láminas"),
                    RecipeIngredient("Brócoli y vainitas", scaleGrams(100, factor1), "al vapor"),
                    RecipeIngredient("Jugo de limón sutil y ajo molido", 15, "al gusto")
                ),
                steps = listOf(
                    "Sazona el filete de pechuga de pollo con ajo, sal, pimienta, comino y unas gotas de limón.",
                    "Dora el pollo en una sartén antiadherente caliente por 4 a 5 minutos por lado hasta que quede jugoso.",
                    "Sirve acompañado de la quinua previamente lavada y sancochada por 15 minutos con verduras al vapor.",
                    "Agrega la palta fuerte en láminas encima de la ensalada justo antes de servir."
                )
            ),
            SuggestedMeal(
                id = "rec_ceviche_fit_camote",
                title = "Ceviche Fit de Pescado Blanco con Camote y Choclo",
                description = "Pescado fresco marinado en limón sutil con camote amarillo sancochado y choclo tierno.",
                prepTimeMinutes = 15,
                calories = (390 * factor2).roundToInt(),
                proteinGrams = (45f * factor2).coerceAtLeast(30f),
                carbsGrams = (38f * factor2).coerceAtLeast(22f),
                fatGrams = (4.5f * factor2).coerceAtLeast(3f),
                mealType = MealType.LUNCH,
                reasonForGoal = "Proteína marina ultra magra con casi cero grasa saturada y carbohidratos complejos del camote.",
                ingredients = listOf(
                    RecipeIngredient("Filete de pescado blanco (perico/lenguado/tilapia)", scaleGrams(210, factor2), "en cubos"),
                    RecipeIngredient("Camote amarillo sancochado", scaleGrams(110, factor2), "en rodajas"),
                    RecipeIngredient("Choclo tierno desgranado", scaleGrams(65, factor2), "sancochado"),
                    RecipeIngredient("Cebolla morada en pluma", scaleGrams(60, factor2), "lavada en agua fría"),
                    RecipeIngredient("Jugo de limón recién exprimido, culantro y ají limo", 45, "fresco")
                ),
                steps = listOf(
                    "Corta el pescado frío en cubos de 2 cm, colócalo en un bol con sal y ají limo picado.",
                    "Vierte el jugo de limón recién exprimido (sin exprimir hasta el fondo para evitar amargor) y mezcla por 1 minuto.",
                    "Incorpora la cebolla morada en pluma y el culantro fresco picado.",
                    "Sirve de inmediato acompañado de las rodajas de camote amarillo y el choclo desgranado."
                )
            ),
            SuggestedMeal(
                id = "rec_sudado_pescado_papa",
                title = "Sudado de Pescado (Bonito/Cabrilla) con Papa Sancochada",
                description = "Cocción al vapor en su propio jugo con tomate, cebolla, kión y papa blanca.",
                prepTimeMinutes = 20,
                calories = (410 * factor3).roundToInt(),
                proteinGrams = (44f * factor3).coerceAtLeast(30f),
                carbsGrams = (36f * factor3).coerceAtLeast(20f),
                fatGrams = (8f * factor3).coerceAtLeast(5f),
                mealType = MealType.DINNER,
                reasonForGoal = "Rico en Omega-3 antiinflamatorio y de digestión muy ligera para la noche en déficit calórico.",
                ingredients = listOf(
                    RecipeIngredient("Filete de bonito, cabrilla o reineta", scaleGrams(200, factor3), "limpio"),
                    RecipeIngredient("Papa blanca o amarilla sancochada", scaleGrams(140, factor3), "en mitades"),
                    RecipeIngredient("Tomate y cebolla morada en gajos", scaleGrams(120, factor3), "para la base"),
                    RecipeIngredient("Kión (jengibre) rallado, ajo y culantro", 15, "aromáticos")
                ),
                steps = listOf(
                    "En una olla o sartén profunda, coloca una cama de cebolla en gajos, tomate, ajo y una pizca de kión rallado.",
                    "Coloca encima el filete de pescado salpimentado, agrega 1/3 de taza de caldo o agua, tapa y cocina a fuego medio por 10 minutos.",
                    "Espolvorea culantro picado al apagar el fuego y acompaña con la papa sancochada."
                )
            ),
            SuggestedMeal(
                id = "rec_tortilla_claras_palta",
                title = "Omelette Proteico de Claras, Huevo, Espinaca y Palta",
                description = "Rápido para desayuno o cena baja en carbohidratos con pan integral.",
                prepTimeMinutes = 10,
                calories = (310 * factor4).roundToInt(),
                proteinGrams = (31f * factor4).coerceAtLeast(22f),
                carbsGrams = (18f * factor4).coerceAtLeast(10f),
                fatGrams = (12f * factor4).coerceAtLeast(7f),
                mealType = MealType.BREAKFAST,
                reasonForGoal = "Alto efecto térmico proteico que protege el músculo mientras oxidas grasa.",
                ingredients = listOf(
                    RecipeIngredient("Claras de huevo", scaleGrams(140, factor4), "aprox. 4 claras"),
                    RecipeIngredient("Huevo entero", 55, "1 unidad"),
                    RecipeIngredient("Queso fresco serrano bajo en sal", scaleGrams(35, factor4), "en dados"),
                    RecipeIngredient("Palta fuerte", scaleGrams(45, factor4), "en tajadas"),
                    RecipeIngredient("Espinaca bebé fresca", 40, "1 taza")
                ),
                steps = listOf(
                    "Bate el huevo entero con las claras y una pizca de sal y orégano.",
                    "Saltea la espinaca por 1 minuto en sartén antiadherente, vierte los huevos batidos y añade el queso fresco.",
                    "Dobla por la mitad cuando cuaje y sirve acompañado de la palta en tajadas."
                )
            )
        )
    } else {
        val factor1 = (targetMealCals / 610f).coerceIn(0.8f, 1.5f)
        val factor2 = (targetMealCals / 580f).coerceIn(0.8f, 1.5f)
        val factor3 = (targetMealCals / 560f).coerceIn(0.8f, 1.5f)
        val factor4 = (targetMealCals / 520f).coerceIn(0.8f, 1.45f)

        listOf(
            SuggestedMeal(
                id = "rec_lomo_saltado_fit",
                title = "Lomo Saltado Fit con Papa Amarilla al Horno y Arroz",
                description = "Clásico limeño adaptado al gimnasio: res magra salteada al wok con mínima grasa y doble fuente de energía.",
                prepTimeMinutes = 20,
                calories = (610 * factor1).roundToInt(),
                proteinGrams = (52f * factor1).coerceAtLeast(38f),
                carbsGrams = (66f * factor1).coerceAtLeast(45f),
                fatGrams = (14f * factor1).coerceAtLeast(9f),
                mealType = MealType.LUNCH,
                reasonForGoal = "Aporta hierro hemo, creatina natural de la res y carbohidratos complejos para superávit limpio e hipertrofia.",
                ingredients = listOf(
                    RecipeIngredient("Lomo fino o bistec de res magro", scaleGrams(200, factor1), "en tiras"),
                    RecipeIngredient("Papa amarilla o blanca al horno/airfryer", scaleGrams(160, factor1), "en bastones"),
                    RecipeIngredient("Arroz blanco o integral graneado", scaleGrams(130, factor1), "cocido"),
                    RecipeIngredient("Cebolla morada, tomate y ají amarillo", scaleGrams(110, factor1), "en gajos"),
                    RecipeIngredient("Sillao bajo en sodio y vinagre tinto", 15, "para el salteado")
                ),
                steps = listOf(
                    "Corta la papa en bastones y hornéala (o en AirFryer) a 200°C por 15 minutos con una pizca de sal.",
                    "Calienta una sartén o wok a fuego alto, sella las tiras de res magra por 2 minutos para mantener sus jugos.",
                    "Agrega la cebolla morada, ají amarillo en tiras, tomate, un chorrito de vinagre y sillao; saltea 1 minuto más.",
                    "Espolvorea culantro fresco picado y sirve con la papa dorada y el arroz."
                )
            ),
            SuggestedMeal(
                id = "rec_lentejas_pollo_camote",
                title = "Lentejas Caseras con Pechuga a la Plancha y Palta",
                description = "Lunes de menestra peruana recargado en proteína para volumen muscular sin grasa excesiva.",
                prepTimeMinutes = 20,
                calories = (580 * factor2).roundToInt(),
                proteinGrams = (54f * factor2).coerceAtLeast(38f),
                carbsGrams = (62f * factor2).coerceAtLeast(42f),
                fatGrams = (12f * factor2).coerceAtLeast(8f),
                mealType = MealType.LUNCH,
                reasonForGoal = "Combinación sinérgica de carbohidratos de absorción lenta (lentejas) y leucina de la pechuga de pollo.",
                ingredients = listOf(
                    RecipeIngredient("Pechuga de pollo en filete", scaleGrams(200, factor2), "a la plancha"),
                    RecipeIngredient("Lentejas cocidas guisadas con ajo y cebolla", scaleGrams(180, factor2), "cocidas"),
                    RecipeIngredient("Arroz graneado o quinua", scaleGrams(100, factor2), "cocido"),
                    RecipeIngredient("Palta fuerte", scaleGrams(55, factor2), "en tajadas")
                ),
                steps = listOf(
                    "Adereza las lentejas cocidas con un sofrito ligero de cebolla picada, ajo, comino y pimentón.",
                    "Cocina la pechuga de pollo a la plancha con sal, pimienta y orégano durante 4-5 minutos por lado.",
                    "Sirve las lentejas con la porción de arroz o quinua, el pollo encima y la palta más zarza criolla."
                )
            ),
            SuggestedMeal(
                id = "rec_pollo_camote_mani",
                title = "Bowl Anabólico de Pollo, Camote Amarillo, Quinua y Palta",
                description = "Energía sostenida post-entrenamiento para reponer glucógeno muscular rápidamente.",
                prepTimeMinutes = 15,
                calories = (560 * factor3).roundToInt(),
                proteinGrams = (50f * factor3).coerceAtLeast(36f),
                carbsGrams = (60f * factor3).coerceAtLeast(40f),
                fatGrams = (13f * factor3).coerceAtLeast(8f),
                mealType = MealType.DINNER,
                reasonForGoal = "El camote amarillo y la quinua rellenan los depósitos de glucógeno mientras los 50g de proteína reparan fibras.",
                ingredients = listOf(
                    RecipeIngredient("Pechuga de pollo en cubos", scaleGrams(190, factor3), "salteada"),
                    RecipeIngredient("Camote amarillo sancochado o al horno", scaleGrams(170, factor3), "en cubos"),
                    RecipeIngredient("Quinua cocida", scaleGrams(110, factor3), "graneada"),
                    RecipeIngredient("Palta fuerte", scaleGrams(60, factor3), "en cubos")
                ),
                steps = listOf(
                    "Sancocha u hornea el camote amarillo en cubos hasta que esté tierno.",
                    "Saltea los cubos de pechuga de pollo con ajo, páprika y sal en sartén caliente por 7 minutos.",
                    "Arma el bowl con una base de quinua y camote, añade el pollo caliente y corona con la palta."
                )
            ),
            SuggestedMeal(
                id = "rec_avena_quinua_mani_platano",
                title = "Porridge Peruano de Avena, Quinua, Plátano de Seda y Maní",
                description = "Desayuno o merienda hipercalórica limpia con proteína, fruta y mantequilla de maní.",
                prepTimeMinutes = 8,
                calories = (520 * factor4).roundToInt(),
                proteinGrams = (36f * factor4).coerceAtLeast(28f),
                carbsGrams = (64f * factor4).coerceAtLeast(45f),
                fatGrams = (14f * factor4).coerceAtLeast(9f),
                mealType = MealType.BREAKFAST,
                reasonForGoal = "Ideal antes o después de entrenar pesas: energía rápida del plátano de seda y sostenida de la avena con maní.",
                ingredients = listOf(
                    RecipeIngredient("Hojuelas de avena y quinua", scaleGrams(70, factor4), "en seco"),
                    RecipeIngredient("Proteína Whey o leche alta en proteína", scaleGrams(32, factor4), "1 scoop"),
                    RecipeIngredient("Plátano de seda maduro", scaleGrams(110, factor4), "en rodajas"),
                    RecipeIngredient("Mantequilla de maní 100% natural", scaleGrams(22, factor4), "1 cucharada colmada"),
                    RecipeIngredient("Canela en polvo", 3, "al gusto")
                ),
                steps = listOf(
                    "Cocina la avena y quinua con 220 ml de agua o leche y canela a fuego medio por 5 minutos hasta espesar.",
                    "Retira del fuego, deja entibiar 1 minuto e incorpora el scoop de proteína revolviendo bien.",
                    "Sirve en un tazón y decora con el plátano de seda en rodajas y la mantequilla de maní encima."
                )
            )
        )
    }
}
