import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

val envFile = rootProject.file("../.env")
val envProperties = Properties()
if (envFile.exists()) {
    envProperties.load(FileInputStream(envFile))
}

android {
    namespace = "it.roadies.android_app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "it.roadies.android_app"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = "roadies"

        val stripeKey = envProperties.getProperty("STRIPE_pk")
        buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"$stripeKey\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.material3)
    val room_version = "2.8.4"
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation("net.openid:appauth:0.11.1")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-android-compiler:2.59.2")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel-compose:1.3.0")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.retrofit2:converter-scalars:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation("tech.utsmankece:osm-android-compose:0.0.5")
    implementation("com.stripe:stripe-android:23.11.0")
}
