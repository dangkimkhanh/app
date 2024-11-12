plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.h2h"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.h2h"
        minSdk = 25
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
    buildFeatures {
        viewBinding = true
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.animation.core)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.compose.material)
    implementation(libs.play.services.fido)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))//firebase
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.1")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.compose.ui:ui:1.3.0")
    implementation("androidx.compose.ui:ui-graphics:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.0")
    implementation("androidx.compose.material3:material3:1.0.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.3.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.3.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.facebook.android:facebook-android-sdk:8.0.0")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("androidx.glance:glance:1.0.0-alpha03")



}