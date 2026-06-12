plugins {
    alias(libs.plugins.android.application)
}

android {
    // IMPORTANTE: Asegúrate de que este namespace coincida con el paquete de tu app
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26 // El driver de PostgreSQL (GSSAPI/MethodHandle) requiere mínimo API 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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


}

dependencies {
    // --- LIBRERÍAS BASE DE ANDROID ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.android.material:material:1.11.0")
    // --- GOOGLE MAPS Y UBICACIÓN GPS ---
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("org.postgresql:postgresql:42.7.2") // Actualizado de 42.6.0 a 42.7.2
    // =======================================================================
    // 🚀 NUEVAS DEPENDENCIAS PARA CONEXIÓN DIRECTA A NEON TECH (POSTGRESQL)
    // =======================================================================

    // Driver Oficial JDBC de PostgreSQL compatible con Android

    // Corrutinas (Necesarias para hacer consultas sin congelar la pantalla)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Ciclo de vida para el lifecycleScope en los Fragments/Activities
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // =======================================================================

    // --- PRUEBAS UNITARIAS ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
