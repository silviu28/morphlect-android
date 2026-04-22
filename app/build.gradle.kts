import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.sil.morphlect"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sil.morphlect"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        buildConfigField(
            "String",
            "UNSPLASH_ACCESS_KEY",
            "\"${properties.getProperty("UNSPLASH_ACCESS_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "UNSPLASH_SECRET_KEY",
            "\"${properties.getProperty("UNSPLASH_SECRET_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "UNSPLASH_APP_ID",
            "\"${properties.getProperty("UNSPLASH_APP_ID", "")}\""
        )
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
        buildConfig = true
    }
    lint {
        disable += "UnusedMaterial3ScaffoldPaddingParameter"
        abortOnError = false
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)

    // https://mvnrepository.com/artifact/org.opencv/opencv
    implementation(libs.opencv)

    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.lifecycle.runtime.ktx.v261)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // https://mvnrepository.com/artifact/org.tensorflow/tensorflow-lite
    implementation(libs.tensorflow.lite)

    // https://mvnrepository.com/artifact/org.tensorflow/tensorflow-lite-support
    implementation(libs.tensorflow.lite.support)

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation(libs.gson)

    // https://mvnrepository.com/artifact/androidx.compose.material/material-icons-extended
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Source: https://mvnrepository.com/artifact/com.google.mlkit/object-detection
    implementation(libs.object1.detection)

    // ListenableFuture deps
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("androidx.concurrent:concurrent-futures:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.0")

    implementation(project(":mxtengine"))
}


