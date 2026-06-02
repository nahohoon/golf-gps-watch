import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(keystorePropertiesFile.inputStream())
    }
}

android {
    namespace = "com.nahohoon.golfgps"
    compileSdk = 34

    base {
        archivesName.set("app")
    }

    defaultConfig {
        applicationId = "com.nahohoon.golfgps"
        minSdk = 30
        targetSdk = 34
        versionCode = 4
        versionName = "1.0.4"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.wear:wear:1.3.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}

tasks.matching { it.name == "signReleaseBundle" }.configureEach {
    doFirst {
        if (keystorePropertiesFile.exists()) {
            val store = keystoreProperties["storeFile"]
            logger.lifecycle("========================================")
            logger.lifecycle("K2 Golf GPS: RELEASE signing (keystore.properties)")
            logger.lifecycle("  storeFile = $store")
            logger.lifecycle("  keyAlias  = ${keystoreProperties["keyAlias"]}")
            logger.lifecycle("========================================")
        } else {
            logger.lifecycle("========================================")
            logger.lifecycle("WARNING: keystore.properties not found.")
            logger.lifecycle("K2 Golf GPS: DEBUG signing fallback for release bundle.")
            logger.lifecycle("Play Console upload requires release keystore signing.")
            logger.lifecycle("  Copy keystore.properties.example -> keystore.properties")
            logger.lifecycle("========================================")
        }
    }
}
