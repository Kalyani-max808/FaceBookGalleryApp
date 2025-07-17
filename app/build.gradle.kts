plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
}

android {
    namespace = "com.facebook.galleryapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.facebook.galleryapp"
        minSdk = 24
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.multidex)
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.material.v190)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    // SQL-ROOM
    val room_version = "1.0.0"
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)

    implementation(libs.gson)

    implementation(libs.androidx.cardview)
    // Admob Ads
    //implementation(libs.play.services.ads)
    // Recycleview
    implementation(libs.androidx.recyclerview)
    // kapt("androidx.lifecycle:lifecycle-compiler:2.3.1")
    implementation(libs.kotlinx.coroutines.android) // Couroutines
    // Kotlin
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material.v1110alpha01)
    // implementation("io.coil-kt:coil:1.2.0")
    // LIFECYCLE COMPONENT
    // implementation("android.arch.lifecycle:extensions:1.0.0")
    // implementation("com.irozon.sneaker:sneaker:2.0.0")
    // implementation("io.github.microutils:kotlin-logging:1.12.5")
    // implementation("net.alexandroid.utils:mylogkt:1.12")
    // implementation("com.github.anrwatchdog:anrwatchdog:1.4.0")
    // fresco library
    implementation(libs.fresco)
    // animation library
    implementation(libs.animated.webp)
    implementation(libs.webpsupport)
    // PHOTO DRAWEE VIEW FOR ZOOM
    implementation(libs.photodraweeview)
    // HTML
    implementation(libs.html.dsl)
    implementation(libs.facebook.audience.network) {
        exclude(group = "com.android.support")
    }
}