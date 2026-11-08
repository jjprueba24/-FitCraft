package com.example.data

data class FoodPreset(
    val name: String,
    val portion: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val defaultMeal: MealType
)

object FoodDatabase {
    val commonFoods = listOf(
        FoodPreset("Avena con Leche o Bebida Vegetal", "80g avena + 200ml", 360, 16f, 58f, 7f, MealType.BREAKFAST),
        FoodPreset("Huevos Revueltos (3 huevos)", "3 unidades medianas", 225, 19f, 2f, 15f, MealType.BREAKFAST),
        FoodPreset("Tostadas Integrales con Aguacate y Tomate", "2 rebanadas + 50g aguacate", 260, 6f, 30f, 13f, MealType.BREAKFAST),
        FoodPreset("Yogur Griego 0% con Frutos Rojos", "200g yogur + 50g arándanos", 160, 20f, 15f, 1f, MealType.BREAKFAST),
        FoodPreset("Pechuga de Pollo a la Plancha", "200g cocinado", 260, 52f, 0f, 5f, MealType.LUNCH),
        FoodPreset("Arroz Blanco o Integral Hervido", "150g cocinado", 195, 4f, 42f, 1f, MealType.LUNCH),
        FoodPreset("Salmón Fresco al Horno", "180g filete", 370, 38f, 0f, 23f, MealType.LUNCH),
        FoodPreset("Boniato o Patata Asada", "200g asado", 180, 4f, 41f, 0.5f, MealType.LUNCH),
        FoodPreset("Ensalada Mediterránea con AOVE", "Bol variado + 10ml aceite oliva", 140, 2f, 8f, 11f, MealType.LUNCH),
        FoodPreset("Batido de Proteína Whey en Agua", "1 cacito (30g)", 120, 24f, 2f, 1.5f, MealType.SNACK),
        FoodPreset("Manzana con Mantequilla de Cacahuete", "1 manzana + 20g crema", 210, 5f, 25f, 10f, MealType.SNACK),
        FoodPreset("Frutos Secos Variados (Nueces/Almendras)", "30 gramos", 185, 6f, 5f, 16f, MealType.SNACK),
        FoodPreset("Filete de Ternera Magra a la Plancha", "180g", 270, 44f, 0f, 9f, MealType.DINNER),
        FoodPreset("Merluza o Bacalao con Verduras Salteadas", "200g pescado + brócoli", 210, 36f, 8f, 3f, MealType.DINNER),
        FoodPreset("Tortilla Francesa de 2 Huevos y Atún", "2 huevos + 1 lata atún natural", 250, 32f, 1f, 12f, MealType.DINNER),
        FoodPreset("Queso Fresco Batido con Canela", "200 gramos", 110, 18f, 8f, 0.5f, MealType.DINNER)
    )
}
