# Reproductor de Música 🎵

Aplicación de reproducción de música para Android desarrollada con **Android Studio** y **Kotlin**.

## Características

- 📋 Lista todas las canciones almacenadas en el dispositivo
- ▶️ Reproducción en primer plano y en segundo plano (Foreground Service)
- ⏸️ Controles de reproducción: play/pause, siguiente, anterior
- 🔄 Barra de progreso con tiempo actual y duración total
- 🔔 Notificación persistente durante la reproducción en segundo plano
- 🎨 Interfaz con Material Design 3

## Estructura del proyecto

```
app/src/main/
├── java/com/example/reproductormusica/
│   ├── Song.kt            # Modelo de datos de canción
│   ├── MusicAdapter.kt    # Adaptador RecyclerView para la lista de canciones
│   ├── MusicService.kt    # Servicio de reproducción en segundo plano
│   ├── MainActivity.kt    # Pantalla principal con lista de canciones
│   └── PlayerActivity.kt  # Pantalla del reproductor con controles
└── res/
    ├── layout/
    │   ├── activity_main.xml    # Layout de la lista de canciones
    │   ├── activity_player.xml  # Layout del reproductor
    │   └── item_song.xml        # Layout de cada ítem en la lista
    └── values/
        ├── strings.xml   # Cadenas de texto en español
        ├── colors.xml    # Paleta de colores
        └── themes.xml    # Tema Material Design
```

## Requisitos

- Android 7.0 (API 24) o superior
- Android Studio Hedgehog (2023.1.1) o superior

## Permisos requeridos

| Permiso | Uso |
|---------|-----|
| `READ_EXTERNAL_STORAGE` | Leer canciones en Android 12 y anteriores |
| `READ_MEDIA_AUDIO` | Leer canciones en Android 13+ |
| `FOREGROUND_SERVICE` | Reproducción en segundo plano |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Tipo de servicio en primer plano (Android 14+) |

## Cómo compilar

1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Sincroniza Gradle (`File > Sync Project with Gradle Files`)
4. Ejecuta la app en un emulador o dispositivo físico con canciones almacenadas
