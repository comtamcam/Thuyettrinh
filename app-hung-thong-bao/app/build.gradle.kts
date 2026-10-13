plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "vn.comtamcam.hungtb"
    compileSdk = 34

    defaultConfig {
        applicationId = "vn.comtamcam.hungtb"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "1.3"
    }

    signingConfigs {
        // Chu ky co dinh (hungtb.keystore trong repo) de moi ban deu cai de duoc len nhau
        getByName("debug") {
            storeFile = rootProject.file("hungtb.keystore")
            storePassword = "hungtb2026"
            keyAlias = "hungtb"
            keyPassword = "hungtb2026"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
