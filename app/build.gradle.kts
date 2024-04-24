plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.pokepoke"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pokepoke"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    //implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.squareup.retrofit2:converter-moshi:2.6.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation ("androidx.compose.runtime:runtime-livedata")

    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.27.0")

    implementation ("com.github.skydoves:landscapist-glide:1.4.7")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation ("com.github.bumptech.glide:compose:1.0.0-alpha.1")
    implementation ("com.github.bumptech.glide:okhttp3-integration:4.11.0")


    implementation ("com.jakewharton.timber:timber:5.0.1")
    implementation ("androidx.navigation:navigation-compose:2.7.7")

    implementation ("androidx.palette:palette-ktx:1.0.0")

    implementation ("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")

    implementation("androidx.palette:palette-ktx:1.0.0")
    //implementation ("com.google.accompanist:accompanist-navigation-animation:0.30.0")

    implementation ("androidx.compose.material:material-icons-extended:1.4.0")

    implementation("androidx.compose.ui:ui:1.7.0-alpha07")
    implementation("androidx.compose.animation:animation-core:1.7.0-alpha07")
    implementation("androidx.compose.animation:animation:1.7.0-alpha07")
    implementation("androidx.compose.foundation:foundation:1.7.0-alpha07")


}