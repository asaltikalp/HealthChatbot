plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.chatbothealth"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.chatbothealth"
        minSdk = 24
        targetSdk = 33
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {
    implementation("androidx.compose.runtime:runtime:1.6.6")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.6")
    implementation("androidx.compose.runtime:runtime-rxjava2:1.6.6")
    implementation ("androidx.compose.material:material-icons-extended:<compose_version>")
    implementation ("io.coil-kt:coil-compose:2.1.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation ("com.aallam.openai:openai-client:3.3.1")
    implementation ("io.ktor:ktor-client-android:2.2.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation ("androidx.activity:activity-compose:1.9.0")
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation ("androidx.compose.ui:ui:<compose_version>")
    implementation ("androidx.compose.ui:ui:<compose_version>")
    implementation ("androidx.compose.material:material:<compose_version>")
    implementation ("androidx.compose.ui:ui-tooling-preview:<compose_version>")
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.ai.client.generativeai:generativeai:0.4.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.11.1")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}