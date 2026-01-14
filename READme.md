# AWS Rekognition Demo - Android App

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)

## 📋 Descripción

Aplicación Android nativa que integra **AWS Rekognition** para análisis facial avanzado. Implementa arquitectura limpia basada en **Domain-Driven Design (DDD)** con **Bounded Contexts**, utilizando **Jetpack Compose** para una UI moderna con **Material Design 3**.

### Funcionalidades Principales

- **🔍 Detección de Rostros**: Análisis facial con detección de emociones y calidad de imagen
- **🤝 Comparación Facial**: Comparación de similitud entre dos rostros con scores de confianza
- **🛡️ Liveness Detection**: Verificación de vida en tiempo real usando AWS Amplify
- **📸 Captura Inteligente**: Integración con CameraX para captura optimizada de imágenes
- **🎨 Material Design 3**: UI/UX moderna con animaciones fluidas

---

## 🏗️ Arquitectura del Proyecto

### Bounded Contexts (DDD)

La aplicación está organizada en **3 contextos delimitados independientes**:

```
┌─────────────────────────────────────────────────────────┐
│                   AWS REKOGNITION DEMO                   │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────┐│
│  │ Face Recognition │  │    Liveness      │  │ Camera ││
│  │    Context       │  │     Context      │  │Context ││
│  ├──────────────────┤  ├──────────────────┤  ├────────┤│
│  │ • Detection      │  │ • Session Mgmt   │  │• Preview││
│  │ • Comparison     │  │ • Verification   │  │• Capture││
│  │ • Emotions       │  │ • AWS Amplify    │  │• Switch ││
│  └──────────────────┘  └──────────────────┘  └────────┘│
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### Capas de Clean Architecture

```
┌─────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                  │
│  (Jetpack Compose + ViewModels + State Management)  │
├─────────────────────────────────────────────────────┤
│                    DOMAIN LAYER                      │
│     (Business Logic + Use Cases + Repositories)      │
├─────────────────────────────────────────────────────┤
│                     DATA LAYER                       │
│  (API Services + DTOs + Mappers + Repository Impl)  │
└─────────────────────────────────────────────────────┘
```

---

## 📁 Estructura del Proyecto

```
com.matfragg.rekognition_demo/
│
├── 📂 shared/                          # Shared Kernel (minimal)
│   ├── 📂 domain/
│   │   └── Result.kt                  # Success/Error wrapper
│   ├── 📂 ui/
│   │   ├── components/                # Reusable UI components
│   │   │   ├── LoadingIndicator.kt
│   │   │   ├── ErrorDialog.kt
│   │   │   └── StatusBanner.kt
│   │   └── theme/                     # Material 3 theming
│   │       ├── Color.kt
│   │       ├── Type.kt
│   │       └── Theme.kt
│   └── 📂 util/
│       ├── Constants.kt
│       └── Extensions.kt
│
├── 📂 domain/                          # Business Logic Layer
│   ├── 📂 face_recognition/
│   │   ├── model/
│   │   │   ├── FaceAnalysis.kt
│   │   │   ├── FaceComparison.kt
│   │   │   └── Emotion.kt
│   │   ├── repository/
│   │   │   └── FaceRecognitionRepository.kt
│   │   └── usecase/
│   │       ├── DetectFaceUseCase.kt
│   │       └── CompareFacesUseCase.kt
│   │
│   ├── 📂 liveness/
│   │   ├── model/
│   │   │   ├── LivenessSession.kt
│   │   │   └── LivenessResult.kt
│   │   ├── repository/
│   │   │   └── LivenessRepository.kt
│   │   └── usecase/
│   │       ├── CreateLivenessSessionUseCase.kt
│   │       └── GetLivenessResultUseCase.kt
│   │
│   ├── 📂 document_ocr/
│   │   ├── model/
│   │   ├── repository/
│   │   └── usecase/
│   │
│   │
│   └── 📂 camera/
│       └── model/
│           └── CapturedImage.kt
│
├── 📂 data/                            # Data Access Layer
│   ├── 📂 face_recognition/
│   │   ├── repository/
│   │   │   └── FaceRecognitionRepositoryImpl.kt
│   │   ├── remote/
│   │   │   ├── RekognitionApi.kt
│   │   │   └── dto/
│   │   │       ├── FaceDetectionDto.kt
│   │   │       └── FaceComparisonDto.kt
│   │   └── mapper/
│   │       └── FaceRecognitionMapper.kt
│   │
│   ├── 📂 liveness/
│   │   ├── repository/
│   │   │   └── LivenessRepositoryImpl.kt
│   │   ├── remote/
│   │   │   ├── LivenessApi.kt
│   │   │   └── dto/
│   │   │       └── LivenessDto.kt
│   │   └── mapper/
│   │       └── LivenessMapper.kt
│   │
│   ├── 📂 document_ocr/
│   │
│   └── 📂 camera/
│       └── util/
│           └── ImageOptimizer.kt
│
├── 📂 presentation/                    # UI Layer (Jetpack Compose)
│   ├── 📂 main/
│   │   ├── MainScreen.kt
│   │   ├── MainViewModel.kt
│   │   └── MainState.kt
│   │
│   ├── 📂 liveness/
│   │   ├── LivenessScreen.kt
│   │   ├── LivenessViewModel.kt
│   │   └── LivenessState.kt
│   │
│   ├── 📂 document_ocr/
│   │
│   └── 📂 camera/
│       ├── CameraPreviewScreen.kt
│       └── components/
│
├── 📂 di/                              # Dependency Injection (Hilt)
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   ├── FaceRecognitionModule.kt
│   ├── LivenessModule.kt
│   └── CameraModule.kt
│
├── 📂 navigation/
│   ├── AppNavGraph.kt
│   └── Screen.kt
│
└── RekognitionApp.kt                  # Application class
```

---

## 🛠️ Tecnologías y Dependencias

### Core Android
| Librería | Versión | Propósito |
|----------|---------|-----------|
| **Kotlin** | 1.9.24 | Lenguaje principal |
| **Compose BOM** | 2024.06.00 | UI declarativa |
| **Material 3** | Latest | Componentes UI modernos |
| **Activity Compose** | 1.9.0 | Integración con Compose |
| **Navigation Compose** | 2.7.7 | Navegación tipo-segura |

### Arquitectura
| Librería | Versión | Propósito |
|----------|---------|-----------|
| **Hilt** | 2.48.1 | Dependency Injection |
| **Lifecycle ViewModel** | 2.6.2 | State management |
| **Coroutines** | 1.7.3 | Asynchronous programming |

### Networking
| Librería | Versión | Propósito |
|----------|---------|-----------|
| **Retrofit** | 2.9.0 | HTTP client |
| **Gson Converter** | 2.9.0 | JSON serialization |
| **OkHttp** | 4.12.0 | HTTP engine |
| **Logging Interceptor** | 4.12.0 | Network debugging |

### Camera
| Librería | Versión | Propósito |
|----------|---------|-----------|
| **CameraX Core** | 1.3.4 | Camera abstraction |
| **CameraX Camera2** | 1.3.4 | Camera2 implementation |
| **CameraX Lifecycle** | 1.3.4 | Lifecycle integration |
| **CameraX View** | 1.3.4 | Preview view |

### AWS Integration
| Librería | Versión | Propósito |
|----------|---------|-----------|
| **AWS Auth Cognito** | 2.19.0 | Authentication |
| **AWS Core** | 2.19.0 | AWS SDK core |
| **AWS Liveness UI** | 1.5.0 | Face liveness detection |

### Testing
| Librería | Versión | Propósito |
|----------|---------|-----------|
| **JUnit** | 4.13.2 | Unit testing |
| **Coroutines Test** | 1.7.3 | Coroutine testing |
| **MockK** | 1.13.8 | Mocking framework |
| **Compose UI Test** | Latest | UI testing |

### Utilities
| Librería | Versión | Propósito |
|----------|---------|-----------|
| **Desugar JDK Libs** | 2.1.5 | Java 8+ APIs en Android < 26 |
| **AppCompat** | Latest | Backward compatibility |
| **Material Components** | Latest | Material Design support |

---

## 🚀 Configuración del Proyecto

### Prerrequisitos

- **Android Studio**: Hedgehog (2023.1.1) o superior
- **JDK**: 11 o superior
- **Gradle**: 8.1+
- **Android SDK**: API 24 (Android 7.0) - API 34 (Android 14)
- **AWS Account**: Con Rekognition y Amplify configurados

### Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/your-username/rekognition-demo.git
cd rekognition-demo
```

2. **Configurar AWS Amplify**

Coloca tu archivo `amplifyconfiguration.json` en:
```
app/src/main/res/raw/amplifyconfiguration.json
```

3. **Actualizar URLs de Lambda**

En `shared/util/Constants.kt`, actualiza:
```kotlin
const val LAMBDA_DETECT_URL = "https://your-detect-lambda.amazonaws.com/"
const val LAMBDA_COMPARE_URL = "https://your-compare-lambda.amazonaws.com/"
```

4. **Sincronizar Gradle**
```bash
./gradlew clean build
```

5. **Ejecutar la aplicación**
```bash
./gradlew installDebug
```

---

## 📱 Uso de la Aplicación

### Pantalla Principal

#### Modo Detección (1 Foto)
1. Alternar el switch a "Modo Detección"
2. Enfocar el rostro en el óvalo morado
3. Presionar el botón de captura
4. Ver resultados: confianza, vida, emociones

#### Modo Comparación (2 Fotos)
1. Mantener el switch en "Modo Comparación"
2. Capturar primera foto
3. Capturar segunda foto
4. Ver resultados: similitud, coincidencia

### Liveness Detection

1. Presionar botón "🛡️ PRUEBA DE VIDA"
2. Seguir instrucciones en pantalla
3. Mantener rostro en el óvalo
4. El sistema detectará automáticamente si es una persona real
5. Ver resultado: REAL ✅ o FALSO ❌

---

## 🏛️ Principios de Arquitectura

### Clean Architecture
- **Dependency Rule**: Las dependencias fluyen hacia adentro
- **Domain Layer**: Sin dependencias de Android
- **Data Layer**: Implementa contratos del dominio
- **Presentation Layer**: Solo conoce el dominio

### Domain-Driven Design
- **Bounded Contexts**: Contextos independientes y cohesivos
- **Ubiquitous Language**: Lenguaje compartido con el negocio
- **Repository Pattern**: Abstracción sobre fuentes de datos
- **Use Cases**: Encapsulan lógica de negocio

### SOLID Principles
- **Single Responsibility**: Una clase, una responsabilidad
- **Open/Closed**: Abierto a extensión, cerrado a modificación
- **Liskov Substitution**: Las interfaces son contratos
- **Interface Segregation**: Interfaces pequeñas y específicas
- **Dependency Inversion**: Depende de abstracciones

---

## 🧪 Testing

### Estructura de Tests

```
test/                          # Unit Tests
├── domain/
│   ├── usecase/
│   └── model/
├── data/
│   ├── repository/
│   └── mapper/
└── presentation/
    └── viewmodel/

androidTest/                   # Integration & UI Tests
├── ui/
│   ├── MainScreenTest.kt
│   └── LivenessScreenTest.kt
└── data/
    └── RepositoryIntegrationTest.kt
```

### Ejecutar Tests

```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew connectedAndroidTest

# Coverage report
./gradlew jacocoTestReport
```

---

## 🔐 Seguridad y Privacidad

- **Permisos**: Solo solicita cámara cuando es necesario
- **Almacenamiento**: Imágenes temporales, eliminadas después de procesamiento
- **AWS Cognito**: Autenticación segura para Liveness
- **HTTPS**: Todas las comunicaciones encriptadas
- **No tracking**: Sin analytics ni recolección de datos personales

---

## 🤝 Contribución

Este es un proyecto de demostración. Si deseas contribuir:

1. Fork el proyecto
2. Crea una branch: `git checkout -b feature/nueva-funcionalidad`
3. Commit cambios: `git commit -m 'Agregar nueva funcionalidad'`
4. Push a la branch: `git push origin feature/nueva-funcionalidad`
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto es de demostración y está disponible bajo la licencia MIT.

---

## 👨‍💻 Autor

**Matfragg**
- GitHub: [@matfragg](https://github.com/matfragg)

---

## 🙏 Agradecimientos

- **AWS Rekognition**: Por la API de reconocimiento facial
- **AWS Amplify**: Por el SDK de Liveness
- **Jetpack Compose**: Por simplificar el desarrollo de UI
- **Material Design 3**: Por los componentes visuales modernos

---

## 📞 Soporte

Para preguntas o problemas:
1. Abre un [Issue](https://github.com/your-username/rekognition-demo/issues)
2. Revisa la [documentación de AWS Rekognition](https://docs.aws.amazon.com/rekognition/)
3. Consulta la [guía de Jetpack Compose](https://developer.android.com/jetpack/compose)

---

## 🗺️ Roadmap

- [ ] Soporte offline con Room Database
- [ ] Historial de análisis
- [ ] Exportación de resultados a PDF
- [ ] Modo oscuro/claro automático
- [ ] Soporte para múltiples idiomas
- [ ] Análisis batch de múltiples imágenes
- [ ] Integración con AWS S3 para almacenamiento
- [ ] Dashboard de estadísticas

---

**¿Listo para construir algo increíble con AWS Rekognition? ¡Clona el repo y comienza! 🚀**