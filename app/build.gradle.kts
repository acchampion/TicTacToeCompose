import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "edu.osu.tictactoecompose"
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.osu.tictactoecompose"
        minSdk = 28
        targetSdk = 37
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
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("11")
    }
}

extensions.findByName("buildScan")?.apply {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "no")
}

configurations {
    create("cleanedAnnotations")
    implementation {
        exclude(group = "com.intellij", module = "annotations")
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
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.core.splashscreen)

    // ViewModels, LiveData, and dependencies
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)

    // Preferences and DataStore
    implementation(libs.androidx.preferences)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)

    // Room
    implementation(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.material)
    implementation(libs.desugar.jdk.libs)
    ksp(libs.androidx.room.compiler)

    // Media3 and ExoPlayer
    implementation(libs.androidx.media3.media3.exoplayer)
    implementation(libs.androidx.media3.media3.exoplayer.dash)
    implementation(libs.androidx.media3.media3.ui)
    implementation(libs.androidx.media3.media3.session)
    implementation(libs.androidx.media3.media3.exoplayer.hls)
    implementation(libs.androidx.mediarouter.mediarouter)

    // Protobuf and Guava for Media3
    implementation(libs.com.google.protobuf.java)
    implementation(libs.org.jetbrains.kotlinx.coroutines.guava)

    // Timber, Retrofit 2, Moshi
    implementation(libs.timber)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.io.coilkt.coil3.coil.compose)
    implementation(libs.io.coilkt.coil3.coil.network.okhttp)

    // Mapbox and Google Accompanist (Permissions)
    implementation(libs.com.mapbox.maps.android.ndk27) {
        exclude(group = "com.google.android.gms", module = "play-services-cronet")
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
    }
    implementation(libs.com.mapbox.extension.maps.compose.ndk27)
    implementation(libs.org.chromium.net.cronet.embedded) {
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
    }
    //implementation(libs.com.google.accompanist.permissions)

    // Testing: JUnit and Espresso
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // UI Testing
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // LeakCanary
    debugImplementation(libs.leakcanary.android)
}