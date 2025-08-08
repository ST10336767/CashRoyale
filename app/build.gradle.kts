plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.cashroyale"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cashroyale"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navigation components
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Android Lifecycle components - CONSOLIDATED to single, latest versions
    implementation(libs.androidx.lifecycle.livedata.ktx) // This typically brings in LiveData and ViewModel KTX
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // Explicitly good to have
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // Use the latest provided, 2.7.0

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.legacy.support.v4)

    // Picasso (Image loading library)
    implementation("com.squareup.picasso:picasso:2.8")


    // Firebase - Ensure platform BOM is at the top of Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation(libs.firebase.firestore.ktx) // From libs.versions.toml
    implementation(libs.firebase.auth.ktx)     // From libs.versions.toml

    // Kotlin Reflect - Only needed if you are doing reflective operations. Remove if not explicitly used.
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0") // Aligning with common Kotlin versions for now. Could be 2.x if your Kotlin plugin is 2.x

    // Kotlin Coroutines - CONSOLIDATED to single, latest versions
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Use the latest, 1.7.3
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Use the latest, 1.7.3

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    // Palette
    implementation("androidx.palette:palette:1.0.0")
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime.android)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation ("androidx.arch.core:core-testing:2.2.0")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation ("io.mockk:mockk:1.13.10")

    // For OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // For Kotlin Coroutines (if you're using them, highly recommended for network ops)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0") // If on Android

}