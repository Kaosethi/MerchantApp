plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.merchantapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.merchantapp"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx) // Already present, good for lifecycle
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    // Add this line if it's missing // This line was added previously to fix icon issue
    implementation("androidx.compose.material:material-icons-extended:1.6.6")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.7.7") // Or the latest version


    // --- ADDED: Networking Libraries ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit core
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Gson converter for JSON
    // ADDED: OkHttp Logging Interceptor (needed for logging network calls)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // Use a recent stable version

    // --- ADDED: ViewModel and Lifecycle for Compose ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // ViewModel specifically for Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0") // CollectAsStateWithLifecycle helper etc.

    // --- ADDED: Coroutines for background tasks (used by ViewModelScope) ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")


    // --- Test Dependencies ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}