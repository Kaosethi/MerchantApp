plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Ensure this exists if using Compose plugin alias
}

android {
    namespace = "com.example.merchantapp" // Verify namespace
    compileSdk = 35 // Or your target SDK

    defaultConfig {
        applicationId = "com.example.merchantapp" // Verify app ID
        minSdk = 24
        targetSdk = 35 // Or your target SDK
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true // Often needed for vector drawables
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // Or version compatible with your Kotlin/Compose
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}" // Common exclusion for compose
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Icons (Core and Extended)
    implementation("androidx.compose.material:material-icons-core:1.6.7") // Use latest stable
    implementation("androidx.compose.material:material-icons-extended:1.6.7") // Use latest stable

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7") // Use latest stable

    // Networking (Retrofit, Gson, OkHttp)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // Or latest stable

    // ViewModel and Lifecycle Compose Helpers
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // Use latest stable
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0") // Use latest stable

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Use latest stable

    // CameraX
    val cameraxVersion = "1.3.3" // Use latest stable
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    //noinspection GradleDependency
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")

    // Accompanist Permissions
    val accompanistVersion = "0.34.0" // Use latest stable
    implementation("com.google.accompanist:accompanist-permissions:${accompanistVersion}")


    // AppCompat (Often needed for themes, compatibility, delegates)
    implementation("androidx.appcompat:appcompat:1.6.1") // Use latest stable

    // Test Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}