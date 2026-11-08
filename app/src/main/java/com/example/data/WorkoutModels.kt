package com.example.data

data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: String, // Pecho, Espalda, Piernas, Hombros, Brazos, Core
    val sets: Int,
    val reps: String, // ej. "8 - 10" o "12 - 15"
    val restSeconds: Int,
    val tip: String,
    val isCompleted: Boolean = false,
    val imageDrawableRes: Int? = null,
    val commonMistakes: String = "",
    val equipmentRequired: String = "Gimnasio / Barra"
)

data class WorkoutDay(
    val dayNumber: Int,
    val dayName: String, // "Lunes", "Martes", etc.
    val title: String, // "Empuje (Pecho, Hombro, Tríceps)"
    val isRestDay: Boolean,
    val targetMuscles: List<String>,
    val focusGoal: FitnessGoal,
    val exercises: List<Exercise>
)

data class WorkoutRoutine(
    val id: String,
    val title: String,
    val subtitle: String,
    val targetGoal: FitnessGoal,
    val daysPerWeek: Int,
    val days: List<WorkoutDay>
)

object WorkoutPlanPresets {

    fun getRoutineFor(goal: FitnessGoal): WorkoutRoutine {
        return when (goal) {
            FitnessGoal.LOSE_WEIGHT -> cutRoutine
            FitnessGoal.GAIN_MUSCLE -> bulkRoutine
            FitnessGoal.MAINTAIN -> maintainRoutine
        }
    }

    // Rutina orientada a quemar grasa manteniendo músculo (Alta intensidad, superseries / circuitos y cardio moderado)
    val cutRoutine = WorkoutRoutine(
        id = "routine_cut",
        title = "Definición y Quema Grasa Extrema",
        subtitle = "Rutina híbrida hipertrofia + densidad metabólica (Déficit)",
        targetGoal = FitnessGoal.LOSE_WEIGHT,
        daysPerWeek = 5,
        days = listOf(
            WorkoutDay(
                dayNumber = 1,
                dayName = "Lunes",
                title = "Tren Superior: Fuerza & Densidad",
                isRestDay = false,
                targetMuscles = listOf("Pecho", "Espalda", "Hombros"),
                focusGoal = FitnessGoal.LOSE_WEIGHT,
                exercises = listOf(
                    Exercise("e1", "Press de Banca con Mancuernas o Barra", "Pecho", 4, "10-12", 60, "Baja controlado en 3 segundos y empuja con potencia.", imageDrawableRes = com.example.R.drawable.guide_bench_press_1794156650505, commonMistakes = "Abrir los codos a 90° (daña el hombro); despegar los glúteos del banco."),
                    Exercise("e2", "Dominadas o Jalón al Pecho", "Espalda", 4, "10-12", 60, "Retrae escápulas y mantén el pecho erguido.", imageDrawableRes = com.example.R.drawable.guide_pullups_1794156676011, commonMistakes = "Balancearse con las piernas; no completar el rango bajando del todo."),
                    Exercise("e3", "Press Militar con Mancuernas", "Hombros", 3, "12", 60, "No arquees la zona lumbar al empujar."),
                    Exercise("e4", "Remo con Mancuerna a 1 Brazo", "Espalda", 3, "12 cada lado", 45, "Lleva el codo hacia la cadera."),
                    Exercise("e5", "Fondos en Paralelas o Máquina", "Pecho/Tríceps", 3, "12-15", 45, "Inclina el torso ligeramente hacia adelante."),
                    Exercise("e6", "HIIT Finisher: Burpees + Cuerda", "Metabólico", 4, "40s ON / 20s OFF", 60, "Máxima intensidad para activar EPOC.")
                )
            ),
            WorkoutDay(
                dayNumber = 2,
                dayName = "Martes",
                title = "Tren Inferior: Potencia & Quema",
                isRestDay = false,
                targetMuscles = listOf("Cuádriceps", "Isquios", "Glúteos"),
                focusGoal = FitnessGoal.LOSE_WEIGHT,
                exercises = listOf(
                    Exercise("e7", "Sentadilla Goblet o con Barra", "Cuádriceps", 4, "10-12", 75, "Profundidad rompiendo el paralelo con el suelo.", imageDrawableRes = com.example.R.drawable.guide_squat_1794156639004, commonMistakes = "Colapsar las rodillas hacia adentro (valgo); despegar los talones del suelo."),
                    Exercise("e8", "Prensa Inclinada de Piernas", "Piernas", 4, "12-15", 60, "No bloquees las rodillas arriba."),
                    Exercise("e9", "Peso Muerto Rumano con Mancuernas", "Isquios/Glúteos", 4, "12", 60, "Siente la tensión en los femorales, espalda neutra."),
                    Exercise("e10", "Zancadas Dinámicas Caminando", "Piernas completas", 3, "15 pasos", 60, "Paso largo manteniendo ángulo de 90°."),
                    Exercise("e11", "Elevación de Talones (Gemelos)", "Gemelos", 4, "20", 45, "Pausa de 2 segundos arriba."),
                    Exercise("e12", "Plancha Abdominal + Mountain Climbers", "Core", 3, "45s + 30s", 45, "Core activo sin que caiga la pelvis.")
                )
            ),
            WorkoutDay(
                dayNumber = 3,
                dayName = "Miércoles",
                title = "Descanso Activo / Recuperación",
                isRestDay = true,
                targetMuscles = emptyList(),
                focusGoal = FitnessGoal.LOSE_WEIGHT,
                exercises = listOf(
                    Exercise("rest1", "Caminata al aire libre o Cinta ligera", "Cardio LISS", 1, "40-50 min", 0, "Ritmo conversacional (Zona 2) para oxidar grasa y no fatigar el SNC."),
                    Exercise("rest2", "Sesión de Movilidad y Foam Roller", "Recuperación", 1, "15 min", 0, "Enfócate en cadera, tobillos y columna torácica.")
                )
            ),
            WorkoutDay(
                dayNumber = 4,
                dayName = "Jueves",
                title = "Torso y Brazos Metabólico",
                isRestDay = false,
                targetMuscles = listOf("Brazos", "Pecho", "Hombros"),
                focusGoal = FitnessGoal.LOSE_WEIGHT,
                exercises = listOf(
                    Exercise("e13", "Press Inclinado con Mancuernas", "Pecho Superior", 4, "12", 60, "Banca a 30 grados para enfatizar haz clavicular."),
                    Exercise("e14", "Remo en Polea Baja (Gironda)", "Espalda Media", 4, "12", 60, "Aprieta las escápulas 1 segundo en cada contracción."),
                    Exercise("e15", "Superserie: Curl de Bíceps con Barra + Extensión Tríceps Polea", "Brazos", 4, "12 + 12", 45, "Sin pausa entre bíceps y tríceps."),
                    Exercise("e16", "Elevaciones Laterales con Mancuerna", "Hombro Lateral", 4, "15", 45, "Codos guiando el movimiento, peso moderado."),
                    Exercise("e17", "Cardio Sprint en Cinta o Bici", "Cardio HIIT", 6, "30s sprint / 30s suave", 60, "Vaciado de glucógeno y aceleración metabólica.")
                )
            ),
            WorkoutDay(
                dayNumber = 5,
                dayName = "Viernes",
                title = "Piernas Glúteos y Abdomen Explosivo",
                isRestDay = false,
                targetMuscles = listOf("Glúteos", "Femorales", "Abdominales"),
                focusGoal = FitnessGoal.LOSE_WEIGHT,
                exercises = listOf(
                    Exercise("e18", "Hip Thrust con Barra o Mancuerna", "Glúteo Mayor", 4, "12", 75, "Bloqueo firme arriba mirando al frente."),
                    Exercise("e19", "Curl Femoral Tumbado o Sentado", "Isquios", 4, "12-15", 60, "Fase excéntrica muy controlada."),
                    Exercise("e20", "Extensión de Cuádriceps", "Cuádriceps", 3, "15-20", 45, "Quema metabólica acumulando metabolitos."),
                    Exercise("e21", "Elevación de Piernas Colgado", "Abdomen Inferior", 3, "15", 45, "Evita el balanceo con el cuerpo."),
                    Exercise("e22", "Rueda Abdominal o Plancha con Desplazamiento", "Core Antiextensión", 3, "12", 45, "Glúteos apretados todo el tiempo.")
                )
            ),
            WorkoutDay(
                dayNumber = 6,
                dayName = "Sábado",
                title = "Cardio LISS y Movilidad",
                isRestDay = true,
                targetMuscles = listOf("Cardio"),
                focusGoal = FitnessGoal.LOSE_WEIGHT,
                exercises = listOf(
                    Exercise("e23", "Bicicleta Estática o Caminata Inclinada", "Cardio Zona 2", 1, "45 min", 0, "Frecuencia cardíaca entre 120-140 lpm.")
                )
            ),
            WorkoutDay(
                dayNumber = 7,
                dayName = "Domingo",
                title = "Descanso Total y Regeneración",
                isRestDay = true,
                targetMuscles = emptyList(),
                focusGoal = FitnessGoal.LOSE_WEIGHT,
                exercises = listOf(
                    Exercise("rest_sun", "Nutrición, Hidratación y Sueño Reparador", "Recuperación", 1, "8 horas sueño", 0, "Día para recargar glucógeno y descansar tendones.")
                )
            )
        )
    )

    // Rutina orientada a hipertrofia pura y ganancia muscular (Fuerza + Volumen óptimo, descansos más amplios)
    val bulkRoutine = WorkoutRoutine(
        id = "routine_bulk",
        title = "Hipertrofia Masiva & Fuerza",
        subtitle = "Rutina Push/Pull/Legs dividida con sobrecarga progresiva",
        targetGoal = FitnessGoal.GAIN_MUSCLE,
        daysPerWeek = 5,
        days = listOf(
            WorkoutDay(
                dayNumber = 1,
                dayName = "Lunes",
                title = "Push A: Pecho, Deltoides Anterior y Tríceps",
                isRestDay = false,
                targetMuscles = listOf("Pecho", "Hombros", "Tríceps"),
                focusGoal = FitnessGoal.GAIN_MUSCLE,
                exercises = listOf(
                    Exercise("b1", "Press de Banca Plano con Barra", "Pecho", 4, "6 - 8", 120, "Fuerza base. Pausa de 1 seg sobre el esternón y empuja.", imageDrawableRes = com.example.R.drawable.guide_bench_press_1794156650505, commonMistakes = "Rebotar la barra en las costillas; no usar las piernas para estabilizar (leg drive)."),
                    Exercise("b2", "Press Inclinado con Mancuernas (30°)", "Pecho Superior", 4, "8 - 10", 90, "Rango completo de recorrido sin chocar mancuernas."),
                    Exercise("b3", "Cruces de Polea en Media Altura", "Pecho Aislamiento", 3, "12 - 15", 60, "Siente la contracción isométrica máxima en el centro."),
                    Exercise("b4", "Press Militar con Barra de Pie", "Deltoides", 4, "6 - 8", 90, "Aprieta glúteos y core firmemente al levantar."),
                    Exercise("b5", "Elevaciones Laterales con Cable", "Deltoide Lateral", 4, "12 - 15", 60, "Tensión constante en toda la curva de fuerza."),
                    Exercise("b6", "Press Francés con Barra Z", "Tríceps", 3, "8 - 10", 75, "Baja la barra hacia la frente/coronilla controlando.")
                )
            ),
            WorkoutDay(
                dayNumber = 2,
                dayName = "Martes",
                title = "Pull A: Espalda Completa, Trapecios y Bíceps",
                isRestDay = false,
                targetMuscles = listOf("Espalda", "Bíceps", "Deltoide Posterior"),
                focusGoal = FitnessGoal.GAIN_MUSCLE,
                exercises = listOf(
                    Exercise("b7", "Dominadas Lastradas o Libres", "Dorsales", 4, "6 - 8", 120, "Tracciona con los codos hacia abajo, barbilla sobre la barra.", imageDrawableRes = com.example.R.drawable.guide_pullups_1794156676011, commonMistakes = "Encoger los hombros hacia las orejas; patear con las rodillas para subir."),
                    Exercise("b8", "Remo con Barra Prono (Pendlay)", "Espalda Densidad", 4, "6 - 8", 120, "Espalda paralela al suelo, explosivo al subir."),
                    Exercise("b9", "Remo en Polea Unilateral con Agarre Neutro", "Dorsal Ancho", 3, "10 - 12", 60, "Estiramiento profundo en cada repetición."),
                    Exercise("b10", "Face Pulls con Cuerda", "Posterior/Manguito", 4, "15 - 20", 60, "Gira las muñecas hacia atrás a la altura de las orejas."),
                    Exercise("b11", "Curl de Bíceps Inclinado con Mancuernas", "Bíceps (Cabeza Larga)", 3, "10 - 12", 60, "Estiramiento máximo del bíceps en el punto bajo."),
                    Exercise("b12", "Curl Martillo con Mancuernas", "Braquiorradial/Bíceps", 3, "10 - 12", 60, "Grosor para el antebrazo y brazo superior.")
                )
            ),
            WorkoutDay(
                dayNumber = 3,
                dayName = "Miércoles",
                title = "Legs A: Dominancia de Cuádriceps y Pantorrillas",
                isRestDay = false,
                targetMuscles = listOf("Cuádriceps", "Aductores", "Gemelos"),
                focusGoal = FitnessGoal.GAIN_MUSCLE,
                exercises = listOf(
                    Exercise("b13", "Sentadilla Trasera con Barra", "Pierna Completa", 4, "6 - 8", 150, "Céntrate en bajar estable y mantener el centro de gravedad.", imageDrawableRes = com.example.R.drawable.guide_squat_1794156639004, commonMistakes = "Bajar el pecho hacia el suelo (buenos días involuntario); apoyar el peso en la punta de los pies."),
                    Exercise("b14", "Sentadilla Hack o Prensa Inclinada", "Cuádriceps", 4, "8 - 10", 90, "Pies abajo para maximizar flexión de rodilla."),
                    Exercise("b15", "Sentadilla Búlgara con Mancuernas", "Glúteo/Cuádriceps", 3, "10 cada lado", 90, "Baja profundo manteniendo la tibia vertical."),
                    Exercise("b16", "Extensión de Cuádriceps en Máquina", "Aislamiento Cuádriceps", 3, "12 - 15 (Drop set final)", 60, "Aprieta 2 segundos en la máxima extensión."),
                    Exercise("b17", "Elevación de Talones en Prensa", "Gemelos", 4, "12 - 15", 60, "Pausa de 2 segundos en el estiramiento profundo.")
                )
            ),
            WorkoutDay(
                dayNumber = 4,
                dayName = "Jueves",
                title = "Descanso y Alimentación Anabólica",
                isRestDay = true,
                targetMuscles = emptyList(),
                focusGoal = FitnessGoal.GAIN_MUSCLE,
                exercises = listOf(
                    Exercise("rest_bulk1", "Descanso Muscular e Hidratación Óptima", "Recuperación", 1, "Día completo", 0, "Come suficiente carbohidrato y proteína para sintetizar músculo."),
                    Exercise("rest_bulk2", "Movilidad Articular suave", "Movilidad", 1, "15 min", 0, "Apertura de cadera y hombros.")
                )
            ),
            WorkoutDay(
                dayNumber = 5,
                dayName = "Viernes",
                title = "Torso Potencia (Pecho + Espalda + Brazos)",
                isRestDay = false,
                targetMuscles = listOf("Pecho", "Espalda", "Brazos"),
                focusGoal = FitnessGoal.GAIN_MUSCLE,
                exercises = listOf(
                    Exercise("b18", "Press Inclinado con Barra", "Pecho Superior", 4, "8", 90, "Clave para el aspecto rocoso del pecho."),
                    Exercise("b19", "Remo con Mancuerna en Banco Inclinado", "Espalda", 4, "10", 90, "Cero inercia, puro trabajo de espalda media."),
                    Exercise("b20", "Fondos en Paralelas con Lastre", "Tríceps/Pecho", 3, "8 - 10", 75, "Rango amplio y codos pegados para tríceps."),
                    Exercise("b21", "Superserie Brazo: Curl Scott (Predicador) + Press Francés", "Brazos", 4, "10 + 10", 60, "Congestión total.")
                )
            ),
            WorkoutDay(
                dayNumber = 6,
                dayName = "Sábado",
                title = "Legs B: Isquios, Glúteos y Abdomen",
                isRestDay = false,
                targetMuscles = listOf("Isquios", "Glúteos", "Core"),
                focusGoal = FitnessGoal.GAIN_MUSCLE,
                exercises = listOf(
                    Exercise("b22", "Peso Muerto Convencional o Semirrígido", "Cadena Posterior", 4, "5", 150, "Agarre firme, barra pegada a las espinillas.", imageDrawableRes = com.example.R.drawable.guide_deadlift_1794156663816, commonMistakes = "Redondear la espalda lumbar; dejar que la barra se aleje del cuerpo."),
                    Exercise("b23", "Hip Thrust Pesado", "Glúteos", 4, "8 - 10", 90, "Empuje desde los talones con fuerza explosiva."),
                    Exercise("b24", "Curl Femoral Tumbado", "Isquios", 4, "10 - 12", 60, "Fase excéntrica de 3 segundos."),
                    Exercise("b25", "Crunch en Polea Alta (Arrodillado)", "Abdominales", 4, "15", 45, "Flexiona la columna sin usar los flexores de cadera.")
                )
            ),
            WorkoutDay(
                dayNumber = 7,
                dayName = "Domingo",
                title = "Descanso Total",
                isRestDay = true,
                targetMuscles = emptyList(),
                focusGoal = FitnessGoal.GAIN_MUSCLE,
                exercises = listOf(
                    Exercise("rest_sun2", "Recuperación Total", "Descanso", 1, "Día completo", 0, "Crecimiento muscular ocurre durante el descanso.")
                )
            )
        )
    )

    val maintainRoutine = WorkoutRoutine(
        id = "routine_maintain",
        title = "Acondicionamiento y Salud Integral",
        subtitle = "Full body 3-4 días equilibrado",
        targetGoal = FitnessGoal.MAINTAIN,
        daysPerWeek = 4,
        days = cutRoutine.days.take(4)
    )
}
