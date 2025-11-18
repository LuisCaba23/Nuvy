plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //  CORRECCIÓN: Se aplica el plugin de Compose directamente aquí
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.lccm.nuvy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lccm.nuvy"
        minSdk = 26 // ⚠️ SUGERENCIA: 33 es muy alto, 24-26 es más común
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Necesario para Compose
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    //  IMPORTANTE: Habilita la funcionalidad de Compose
    buildFeatures {
        compose = true
    }
}

dependencies {

    // OkHttp para requests HTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Coroutines para operaciones asíncronas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Dependencias básicas de KTX y Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ✅ ¡AQUÍ! Agrega esta línea para conectar ViewModel con Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // ✅ NUEVO: Dependencia para integrar Compose en una Actividad
    implementation(libs.androidx.activity.compose)

    // ✅ NUEVO: BOM de Compose (gestiona las versiones de las librerías de Compose)
    implementation(platform(libs.androidx.compose.bom))

    // ✅ NUEVO: Dependencias fundamentales de Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // ✅ NUEVO: Dependencia para Material Design 3 (botones, scaffolds, etc.)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose.android)
    // 🗑️ ELIMINADAS: Estas ya no son necesarias para una app 100% Compose
    // implementation(libs.androidx.appcompat)
    // implementation(libs.material)
    // implementation(libs.androidx.constraintlayout)

    // Dependencias de testing (actualizadas para Compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}