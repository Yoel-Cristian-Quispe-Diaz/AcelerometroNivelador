# 📐 Acelerómetro Nivelador

Aplicación Android que funciona como **nivel de burbuja digital**: usa el **acelerómetro** del teléfono para medir la inclinación del dispositivo y mostrar si una superficie está nivelada.

> 🚧 Completa las secciones marcadas con `[...]` con los datos reales de tu app.

## ✨ Características

- Lectura en tiempo real del acelerómetro del dispositivo.
- Indicador visual de nivelación (burbuja / indicador de inclinación).
- [Ángulos de inclinación en grados en los ejes X e Y.]
- [Aviso visual o sonoro cuando la superficie está nivelada.]
- [Calibración del sensor.]
- Interfaz diseñada para dispositivos Android.

## 🧰 Tecnologías

| Componente | Tecnología |
|---|---|
| Plataforma | Android |
| Lenguaje | [Kotlin / Java] |
| Sensor | Acelerómetro (`SensorManager` de Android) |
| Sistema de compilación | Gradle (Kotlin DSL) |
| IDE | Android Studio |
| Interfaz | [XML Layouts / Jetpack Compose] |

## 📁 Estructura del proyecto

```
AcelerometroNivelador/
├── app/                     # Código fuente de la aplicación
├── gradle/                  # Gradle Wrapper
├── build.gradle.kts         # Configuración de Gradle (proyecto)
├── settings.gradle.kts      # Módulos del proyecto
├── gradle.properties        # Propiedades de Gradle
├── gradlew / gradlew.bat    # Scripts del Gradle Wrapper
└── .gitignore
```

## 🚀 Instalación

### Requisitos previos

- [Android Studio](https://developer.android.com/studio) (versión reciente)
- JDK compatible con tu versión de Android Gradle Plugin
- **Un dispositivo físico con acelerómetro.** Los emuladores pueden simular el sensor, pero la experiencia real es en un teléfono.
- Android [minSdk] o superior

### Pasos

1. **Clonar el repositorio**

   ```bash
   git clone https://github.com/Yoel-Cristian-Quispe-Diaz/AcelerometroNivelador.git
   ```

2. **Abrir el proyecto** en Android Studio (`File > Open`) y esperar a que Gradle sincronice.

3. **Ejecutar** la app con el botón ▶️ en tu teléfono (con la depuración USB activada) o en un emulador.

## 📖 Uso

1. Abre la aplicación.
2. Apoya el teléfono sobre la superficie que quieres medir.
3. Observa el indicador: cuando la burbuja queda centrada, la superficie está nivelada.
4. [Describe otras funciones: calibración, cambio de modo, etc.]

> 💡 Para mejores resultados, calibra el sensor sobre una superficie que sepas que está plana.

## ⚙️ Cómo funciona

El acelerómetro mide la aceleración en los tres ejes del teléfono. Con el dispositivo en reposo, esa medición corresponde a la gravedad, y comparando sus componentes se obtiene la inclinación respecto a la horizontal.


## 👤 Autor

**Yoel Cristian Quispe Diaz** — [@Yoel-Cristian-Quispe-Diaz](https://github.com/Yoel-Cristian-Quispe-Diaz)
