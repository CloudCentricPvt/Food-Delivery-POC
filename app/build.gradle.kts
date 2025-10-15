plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.cccinfotech.fooddeliverypoc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cccinfotech.fooddeliverypoc"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Jetpack Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material3:material3:1.2.1")

    // Firebase (managed by BOM)
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))

    // Firebase services
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    //Map
    implementation("com.google.android.libraries.maps:maps:3.1.0-beta")
    implementation("com.google.maps.android:maps-ktx:3.4.0")
    implementation("com.google.maps.android:maps-utils-ktx:3.4.0")
    implementation("com.google.android.gms:play-services-maps:19.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation ("com.google.maps.android:maps-compose:4.3.3")
    // Jetpack Compose BOM (make sure you already use it)
    implementation (platform("androidx.compose:compose-bom:2025.01.01")) // or latest
    // Material 3
    implementation ("androidx.compose.material3:material3")
    // ✅ Material Icons (needed for Visibility / VisibilityOff)
    implementation ("androidx.compose.material:material-icons-extended")

    //Volly
    implementation("com.android.volley:volley:1.2.1")

    configurations.all {
        resolutionStrategy {
            force("com.android.volley:volley:1.2.1")
        }
    }

    implementation("com.google.accompanist:accompanist-permissions:0.36.0")
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("com.razorpay:checkout:1.6.40")
    implementation("com.google.android.libraries.places:places:3.3.0")

    //bio metric
    implementation("androidx.biometric:biometric:1.0.1")

}