# FitCraft • Gym, Nutrición Peruana IA, Música In-App y Sheet de Progreso

**FitCraft Gym & Dieta** (`com.aistudio.fitcraftgym.fytwkp`) es una aplicación nativa Android desarrollada en **Kotlin** y **Jetpack Compose (Material Design 3)** para planificación de entrenamiento con guía visual biomecánica, cálculo nutricional dinámico, generación de recetas peruanas con **Gemini IA (`Firebase AI Logic`)**, reproducción de música integrada **100% dentro de la app** y respaldo automático en una hoja de cálculo (**Sheet CSV**) vinculada al correo verificado del usuario.

---

## Características Principales

### 1. Entrenamiento Inteligente con Guía Visual, Mapa Muscular Interactivo y Suite GymMane Pro
- **Mapa Muscular Anatómico Interactivo (Frontal y Posterior)**: silueta anatómica en `Canvas` donde puedes tocar directamente cualquier grupo muscular (*Pecho, Hombros, Bíceps, Core, Cuádriceps, Espalda/Dorsales, Tríceps, Glúteos, Isquios, Gemelos*) para ver los músculos activos del día y filtrar los ejercicios.
- **Registro de Series Pro (Estilo GymMane)**: dentro de cada ejercicio puedes registrar serie por serie alternando el tipo de serie (**`W` Calentamiento**, **`N` Efectiva**, **`D` Drop Set**, **`F` Al Fallo**, **`S` Superserie**), ajustar peso (`kg`) y repeticiones, ver tu **1RM estimado en vivo** y el volumen total levantado (`kg`).
- **Suite de Herramientas GymMane (`GymManeToolsBottomSheet`)**:
  - **Calculadora Visual de Discos de Barra Olímpica**: selecciona barra de `20 kg`, `15 kg` o `10 kg` y peso objetivo para visualizar en `Canvas` los discos exactos por lado con colores oficiales IWF (`25kg Rojo`, `20kg Azul`, `15kg Amarillo`, `10kg Verde`, `5kg Blanco`, `2.5kg`, `1.25kg`).
  - **Calculadora Científica de 1RM**: calcula tu fuerza máxima con las ecuaciones de *Epley*, *Brzycki* y *Lombardi* y muestra la tabla de cargas del `65%` al `95%`.
  - **Creador de Ejercicios Personalizados**: añade tus propios ejercicios a la rutina con figura animada automática.
  - **Pase Digital QR de Gimnasio y Estante de Medallas**: código de acceso rápido para el torniquete del gimnasio y medallas desbloqueables.
- **Radar de Simetría Muscular (6 Ejes)**: gráfico hexagonal en la pestaña *Progreso* que evalúa el equilibrio entre *Pecho, Espalda, Piernas, Hombros, Brazos y Core*.
- **Rutinas personalizadas** según objetivo (**Bajar de Peso / Definición**, **Ganar Masa Muscular** o **Recomposición Corporal**), **días por semana (3, 4, 5 o 6 días)** y **equipo disponible** (*Gimnasio Completo*, *En Casa / Mancuernas* o *Bandas y Peso Corporal*).
- **Guía paso a paso y demostración en vivo**: cada ejercicio incluye fotografía técnica real y una **figura biomecánica animada en `Canvas`** que muestra el movimiento articular en tiempo real, errores comunes a evitar y temporizador de descanso flotante.

### 2. Reproductor de Música 100% Dentro de la App (Sin Salir de la Pantalla de Entreno)
- **Motor Nativo Media3 (`ExoPlayer`) + Sintetizador Rítmico PCM**: reproduce pistas de entrenamiento libres de derechos en segundo plano mientras entrenas.
- **Reproductor Spotify Embebido In-App (`WebView` Oficial)**: permite conectar y escuchar playlists fitness de Spotify (*Beast Mode, Cardio HIIT, Powerlifting, Mobility Flow*) **directamente dentro de la interfaz de la app**, sin abrir navegadores ni aplicaciones externas.
- **Selector de Música Local del Dispositivo (`audio/*`)**: permite elegir tus propios archivos MP3 o pistas guardadas en el teléfono para reproducirlas dentro de la app.

### 3. Nutrición del Mercado Peruano y Generador de Recetas con Gemini IA
- **Base de Alimentos Peruanos**: incluye *palta fuerte, camote amarillo/morado, quinua cocida, papa blanca/amarilla, pescado bonito, tilapia, pechuga de pollo, lentejas, sangrecita, choclo, plátano de seda, maní y kiwicha*.
- **Cálculo en Tiempo Real (Mifflin-St Jeor)**: calcula automáticamente **TMB**, **Gasto Total (TDEE)**, **IMC** y distribución exacta de macronutrientes (**Proteínas, Carbohidratos y Grasas**).
- **Chef Nutricionista con Gemini (`gemini-3.8-flash` vía Firebase AI Logic)**:
  - Genera recetas personalizadas en formato JSON estructurado según las **calorías y macros restantes del día**.
  - Permite seleccionar tus ingredientes disponibles (*Palta, Camote, Quinua, Pescado, Pollo, Lentejas, Papa, Huevos, Maní, Choclo*) y entrega pesos exactos en gramos y pasos de cocción saludables.
  - Funciona para todos los usuarios sin exponer claves privadas gracias a **Firebase AI Logic + Firebase App Check**.

### 4. Cuenta Verificada con PIN + Sheet Automático de Progreso y Fotos
- **Comprobación de Correo + PIN de Seguridad**:
  - Al vincular un correo electrónico, la app emite un **código de comprobación de 6 dígitos** y solicita un **PIN de seguridad de 4+ dígitos**.
  - Al cerrar sesión y volver a ingresar con tu correo y PIN, **nunca se pierde tu información**: la app restaura automáticamente tu perfil, historial de pesajes, comidas y galería de fotos *Antes y Después*.
- **Hoja de Cálculo Integrada (`UserSheetBottomSheet` + Exportación `.csv`)**:
  - Cada cambio en tu perfil, pesaje, comida o foto actualiza automáticamente tu archivo `sheet_progreso_<correo>.csv` con 4 pestañas:
    1. **Progreso (Pesajes y Medidas)**
    2. **Fotos Antes y Después (Rutas persistentes en almacenamiento interno)**
    3. **Comidas y Macros Diarios**
    4. **Perfil y Metas Calculadas**
  - Puedes visualizar la tabla desde el ícono superior de **Sheet** o exportarla directamente a **Google Sheets / Excel**.
- **Modo Invitado Temporal (4 Horas)**: permite probar todas las funciones sin cuenta durante 4 horas con aviso y temporizador visible.

---

## Arquitectura y Tecnologías

| Capa | Tecnología | Descripción |
| :--- | :--- | :--- |
| **UI & Diseño** | Jetpack Compose + Material 3 | Diseño adaptable oscuro (*Dark Navy & Neon Lime*), gráficos en `Canvas` y hojas modales (`ModalBottomSheet`). |
| **Estado & MVVM** | `FitnessViewModel` + `StateFlow` | Flujos reactivos combinados con `collectAsStateWithLifecycle`. |
| **Persistencia Local** | Room Database (`v2`) + Migraciones | Tablas `user_profile`, `measurements`, `meals`, `photos` y `completed_exercises` con `MIGRATION_1_2` segura. |
| **Persistencia Sheet** | `UserSheetManager` (`.csv` + JSON) | Respaldo instantáneo por cuenta de correo verificada y exportación compatible con Google Sheets. |
| **Inteligencia Artificial** | Firebase AI Logic (`gemini-3.8-flash`) | Generación estructurada (`Schema.obj`) de recetas peruanas con protección `Firebase App Check`. |
| **Audio & Música** | Jetpack Media3 (`ExoPlayer`) + Spotify Embed | Reproducción 100% dentro de la app (Streaming libre de derechos, Spotify embebido y MP3 locales). |

---

## Estructura del Proyecto

```text
app/src/main/java/com/example/
├── MainActivity.kt                        # Punto de entrada, App Check y navegación principal
├── util/
│   ├── AppCheckConfig.kt                  # Configuración de Firebase App Check (Debug + Play Integrity/reCAPTCHA)
│   └── PhotoStorageHelper.kt              # Copia segura de fotos Antes/Después al almacenamiento interno
├── data/
│   ├── UserProfile.kt                     # Modelo de perfil y fórmulas metabólicas (TMB, TDEE, Macros, IMC)
│   ├── UserSheetManager.kt                # Verificación por código de 6 dígitos, PIN y generador de Sheet CSV
│   ├── FitnessRepository.kt               # Repositorio unificado Room + Restauración de instantáneas por cuenta
│   ├── FoodDatabase.kt                    # Catálogo de alimentos e ingredientes del mercado peruano
│   ├── WorkoutModels.kt                   # Rutinas por objetivo, guías técnicas y consejos biomecánicos
│   ├── MusicModels.kt                     # Catálogo de playlists fitness y pistas de entrenamiento
│   ├── local/
│   │   ├── FitnessDatabase.kt             # Base de datos Room v2 con migración automática MIGRATION_1_2
│   │   ├── FitnessDao.kt                  # Consultas reactivas Flow y operaciones CRUD
│   │   └── FitnessEntities.kt             # Entidades SQLite para perfil, medidas, comidas, fotos y ejercicios
│   └── spotify/
│       └── SpotifySdkConnector.kt         # Motor de audio Media3 ExoPlayer, PCM y estado Spotify In-App
└── ui/
    ├── FitnessViewModel.kt                # Lógica de negocio, temporizador, Gemini IA y sincronización Sheet
    ├── screens/
    │   ├── WorkoutScreen.kt               # Pantalla de rutina diaria con reproductor musical fijo
    │   ├── NutritionScreen.kt             # Control de calorías, macros y registro de comidas
    │   ├── ProgressScreen.kt              # Gráficos de evolución y galería fotográfica Antes/Después
    │   └── ProfileScreen.kt               # Calculadora en vivo, selector de equipo y guardado en Sheet
    └── components/
        ├── ExerciseCard.kt                # Tarjeta de ejercicio con figura biomecánica animada en Canvas
        ├── WorkoutMiniMusicPlayer.kt      # Reproductor musical integrado y visor Spotify In-App
        ├── MusicPlaylistsBottomSheet.kt   # Selector de playlists Spotify, pistas libres y MP3 del teléfono
        ├── AiMealSuggestionsBottomSheet.kt# Generador de recetas peruanas con Gemini IA y filtros por ingrediente
        ├── AccountAndGuestSessionCard.kt  # Flujo de verificación de correo (6 dígitos), PIN y acceso al Sheet
        └── UserSheetBottomSheet.kt        # Visor interactivo de 4 pestañas del Sheet de progreso y fotos
```

---

## Compilación y Ejecución

1. **Compilar APK Debug**:
   ```bash
   gradle assembleDebug
   ```
2. **Ejecutar Pruebas Unitarias**:
   ```bash
   gradle :app:testDebugUnitTest
   ```
