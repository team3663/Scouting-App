import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("base")
}

var versionMajor = 4
var versionMinor = 0
var versionPatch = 0

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load (FileInputStream(keystorePropertiesFile))
    }
}

configure<com.android.build.api.dsl.ApplicationExtension> {
    namespace = "com.team3663.scouting_app"
    compileSdk = 37

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.team3663.scouting_app"
        minSdk = 30
        targetSdk = 37
        versionCode = (versionMajor * 10000) + (versionMinor * 100) + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

base {
    archivesName = "CPR-Scout-${versionMajor}.${versionMinor}.${versionPatch}"
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.documentfile)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.qr.generator)
    implementation(libs.preference)
    implementation(libs.google.api.client)
    implementation(libs.google.drive.services)
    implementation(libs.play.services.auth)
    implementation(libs.mssql)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}