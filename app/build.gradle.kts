
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val apiUrl = providers.gradleProperty("API_BASE_URL").orElse("https://inventory-eng.vercel.app").get()

android {
    namespace = "com.eis.inventory"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.eis.inventory"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"
        buildConfigField("String", "API_BASE_URL", "\"" + apiUrl + "\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val ksFile = System.getenv("KEYSTORE_FILE")
            signingConfig = if (!ksFile.isNullOrBlank()) {
                signingConfigs.create("releaseKey") {
                    storeFile = file(ksFile)
                    storePassword = System.getenv("KEYSTORE_PASS")
                    keyAlias = System.getenv("KEY_ALIAS")
                    keyPassword = System.getenv("KEY_PASS")
                }
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
    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
    debugImplementation(libs.androidx.ui.tooling)
}
