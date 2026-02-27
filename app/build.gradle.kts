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
    implementation("org.opencv:opencv:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation("androidx.datastore:datastore-core:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    // https://mvnrepository.com/artifact/org.tensorflow/tensorflow-lite
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    // https://mvnrepository.com/artifact/org.tensorflow/tensorflow-lite-support
    implementation("org.tensorflow:tensorflow-lite-support:0.5.0")
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.13.2")
    // https://mvnrepository.com/artifact/androidx.compose.material/material-icons-extended
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
}


