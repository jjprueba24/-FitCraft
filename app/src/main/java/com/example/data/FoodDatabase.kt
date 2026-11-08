package com.example.data

data class FoodPreset(
    val name: String,
    val portion: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val defaultMeal: MealType,
    val category: String = "Perú / Fitness"
)

object FoodDatabase {
    val commonFoods = listOf(
        // Desayunos Peruanos Fitness
        FoodPreset(
            name = "Avena con Quinua y Plátano de Seda",
            portion = "70g avena/quinua + 1 plátano",
            calories = 365,
            protein = 14f,
            carbs = 64f,
            fat = 6f,
            defaultMeal = MealType.BREAKFAST,
            category = "Desayuno Peruano"
        ),
        FoodPreset(
            name = "Pan Integral con Palta Fuerte y Huevos",
            portion = "2 rebanadas + 60g palta + 2 huevos",
            calories = 340,
            protein = 18f,
            carbs = 28f,
            fat = 17f,
            defaultMeal = MealType.BREAKFAST,
            category = "Desayuno Peruano"
        ),
        FoodPreset(
            name = "Tamalito de Quinua o Humita Ligera + Claras",
            portion = "1 unidad mediana + 3 claras",
            calories = 290,
            protein = 19f,
            carbs = 38f,
            fat = 7f,
            defaultMeal = MealType.BREAKFAST,
            category = "Desayuno Peruano"
        ),
        FoodPreset(
            name = "Huevos Revueltos con Queso Fresco Serrano",
            portion = "3 huevos + 40g queso fresco bajo en sal",
            calories = 275,
            protein = 25f,
            carbs = 3f,
            fat = 18f,
            defaultMeal = MealType.BREAKFAST,
            category = "Desayuno Peruano"
        ),
        FoodPreset(
            name = "Sangrecita de Pollo Salteada con Camote",
            portion = "150g sangrecita + 120g camote sancochado",
            calories = 310,
            protein = 29f,
            carbs = 34f,
            fat = 6f,
            defaultMeal = MealType.BREAKFAST,
            category = "Alto en Hierro"
        ),

        // Almuerzos Peruanos Fitness
        FoodPreset(
            name = "Pechuga de Pollo a la Plancha con Papa y Palta",
            portion = "200g pollo + 150g papa sancochada + 50g palta",
            calories = 460,
            protein = 54f,
            carbs = 35f,
            fat = 11f,
            defaultMeal = MealType.LUNCH,
            category = "Clásico Gym"
        ),
        FoodPreset(
            name = "Ceviche de Pescado con Camote y Choclo",
            portion = "220g pescado blanco + 80g camote + 60g choclo",
            calories = 385,
            protein = 44f,
            carbs = 42f,
            fat = 4f,
            defaultMeal = MealType.LUNCH,
            category = "Marino / Magro"
        ),
        FoodPreset(
            name = "Lentejas Caseras con Pescado a la Plancha",
            portion = "160g lentejas + 180g filete bonito/perico + ensalada",
            calories = 480,
            protein = 49f,
            carbs = 46f,
            fat = 9f,
            defaultMeal = MealType.LUNCH,
            category = "Menestra Peruana"
        ),
        FoodPreset(
            name = "Quinua Graneada con Pollo y Ensalada Criolla",
            portion = "160g quinua cocida + 180g pechuga de pollo",
            calories = 440,
            protein = 50f,
            carbs = 40f,
            fat = 8f,
            defaultMeal = MealType.LUNCH,
            category = "Superalimento"
        ),
        FoodPreset(
            name = "Lomo Saltado Fit (Res Magra y Papa al Horno)",
            portion = "180g lomo fino + cebolla/tomate + 140g papa al horno",
            calories = 490,
            protein = 46f,
            carbs = 38f,
            fat = 14f,
            defaultMeal = MealType.LUNCH,
            category = "Criollo Fit"
        ),
        FoodPreset(
            name = "Ají de Gallina Fit (con Yogur Griego y Papa)",
            portion = "180g pechuga deshilachada + 120g papa amarilla + 1 huevo",
            calories = 450,
            protein = 47f,
            carbs = 34f,
            fat = 13f,
            defaultMeal = MealType.LUNCH,
            category = "Criollo Fit"
        ),
        FoodPreset(
            name = "Camote Amarillo o Morado Sancochado / Horno",
            portion = "200g cocido",
            calories = 175,
            protein = 3.5f,
            carbs = 40f,
            fat = 0.5f,
            defaultMeal = MealType.LUNCH,
            category = "Carbohidrato Limpio"
        ),
        FoodPreset(
            name = "Papa Blanca o Amarilla Sancochada",
            portion = "200g cocida",
            calories = 170,
            protein = 4f,
            carbs = 38f,
            fat = 0.3f,
            defaultMeal = MealType.LUNCH,
            category = "Carbohidrato Limpio"
        ),

        // Meriendas / Post-Entreno
        FoodPreset(
            name = "Plátano de Seda con Mantequilla de Maní",
            portion = "1 plátano + 20g mantequilla de maní 100%",
            calories = 220,
            protein = 6.5f,
            carbs = 28f,
            fat = 10f,
            defaultMeal = MealType.SNACK,
            category = "Pre / Post Entreno"
        ),
        FoodPreset(
            name = "Maní Tostado sin Sal y Pecanas",
            portion = "30 gramos (1 puñado)",
            calories = 180,
            protein = 7f,
            carbs = 5f,
            fat = 15f,
            defaultMeal = MealType.SNACK,
            category = "Grasa Saludable"
        ),
        FoodPreset(
            name = "Yogur Griego Natural con Arándanos y Chía",
            portion = "200g yogur + 50g arándanos + 10g chía",
            calories = 185,
            protein = 21f,
            carbs = 16f,
            fat = 4f,
            defaultMeal = MealType.SNACK,
            category = "Proteico"
        ),
        FoodPreset(
            name = "Batido de Proteína Whey en Agua",
            portion = "1 scoop (30g)",
            calories = 120,
            protein = 24f,
            carbs = 2f,
            fat = 1.5f,
            defaultMeal = MealType.SNACK,
            category = "Suplemento"
        ),

        // Cenas Ligeras y Proteicas
        FoodPreset(
            name = "Sudado de Pescado (Bonito/Cabrilla) con Yuca",
            portion = "200g pescado + tomate/cebolla + 100g yuca",
            calories = 340,
            protein = 42f,
            carbs = 26f,
            fat = 7f,
            defaultMeal = MealType.DINNER,
            category = "Cena Peruana"
        ),
        FoodPreset(
            name = "Atún en Agua con Choclo Desgranado y Palta",
            portion = "1 lata atún + 70g choclo + 50g palta + limón",
            calories = 310,
            protein = 32f,
            carbs = 18f,
            fat = 11f,
            defaultMeal = MealType.DINNER,
            category = "Cena Rápida"
        ),
        FoodPreset(
            name = "Tortilla de 2 Huevos, Claras y Atún con Ensalada",
            portion = "2 huevos + 2 claras + 1/2 lata atún + verduras",
            calories = 265,
            protein = 34f,
            carbs = 5f,
            fat = 12f,
            defaultMeal = MealType.DINNER,
            category = "Bajo en Carbos"
        )
    )
}
