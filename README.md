software

esta vola se la pedi a gemini XD

![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![MLKit](https://img.shields.io/badge/Tool-ML%20Kit-orange.svg)
![GoogleMaps](https://img.shields.io/badge/API-Google%20Maps-red.svg)

Una aplicación Android moderna que integra servicios de geolocalización, mapas interactivos y detección facial mediante inteligencia artificial.

## 🚀 Características

- **Geolocalización en Tiempo Real:** Utiliza `FusedLocationProviderClient` para obtener la ubicación precisa del usuario.
- **Mapas Interactivos:** Integración con Google Maps SDK para visualización de entornos.
- **Detección Facial:** Implementación de Google ML Kit para identificar rostros mediante la cámara.
  ***** esto esta aqui porque la app pide permiso altiro de camara pa despues no estar molestando con lo de los permisos*******
- **Permisos Dinámicos:** Gestión de permisos en tiempo de ejecución para Cámara y Ubicación (Fine/Coarse).

## 🛠️ Tecnologías y Librerías

El proyecto utiliza las siguientes dependencias principales:

*   **Lenguaje:** [Kotlin](https://kotlinlang.org/)
*   **UI:** Material Components, ConstraintLayout, ViewBinding.
*   **Navegación:** Jetpack Navigation Component.
*   **Google Services:**
    *   `play-services-maps`: Visualización de mapas.
    *   `play-services-location`: Rastreo de ubicación.
*   **ML Kit:** `face-detection` para procesamiento de imágenes.
*   `Navigation Component`: Para el flujo entre pantallas.
*   `ViewBinding`: Para una interacción segura con las vistas XML.
  

## 📋 Requisitos Previos

Para ejecutar este proyecto, necesitarás:

1.  **Android Studio** (Hedgehog o superior recomendado).
2.  **SDK de Android** (Nivel de API 34 o superior configurado).
3.  **Google Maps API Key:** Debes obtener una clave en [Google Cloud Console](https://console.cloud.google.com/).
 - Actualmente configurada en el `AndroidManifest.xml`.



## 📸 Permisos Solicitados

La app requiere acceso a:
- `ACCESS_FINE_LOCATION`: Para precisión exacta mediante GPS.
- `ACCESS_COARSE_LOCATION`: Para ubicación aproximada mediante red.
