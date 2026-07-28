plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseVersionCode = providers.gradleProperty("releaseVersionCode")
    .orElse("1")
    .map(String::toInt)
val releaseVersionName = providers.gradleProperty("releaseVersionName")
    .orElse("1.0")

val uploadStoreFile = providers.environmentVariable("NIGHT_SCREEN_UPLOAD_STORE_FILE").orNull
val uploadStorePassword =
    providers.environmentVariable("NIGHT_SCREEN_UPLOAD_STORE_PASSWORD").orNull
val uploadKeyAlias = providers.environmentVariable("NIGHT_SCREEN_UPLOAD_KEY_ALIAS").orNull
val uploadKeyPassword =
    providers.environmentVariable("NIGHT_SCREEN_UPLOAD_KEY_PASSWORD").orNull
val uploadSigningValues = listOf(
    uploadStoreFile,
    uploadStorePassword,
    uploadKeyAlias,
    uploadKeyPassword,
)
val hasUploadSigning = uploadSigningValues.all { !it.isNullOrBlank() }

require(uploadSigningValues.none { !it.isNullOrBlank() } || hasUploadSigning) {
    "Set all NIGHT_SCREEN_UPLOAD_* environment variables, or none of them."
}

android {
    namespace = "nl.msvos.nightscreen"
    compileSdk = 36

    defaultConfig {
        applicationId = "nl.msvos.nightscreen"
        minSdk = 34
        targetSdk = 36
        versionCode = releaseVersionCode.get()
        versionName = releaseVersionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasUploadSigning) {
            create("releaseUpload") {
                storeFile = file(checkNotNull(uploadStoreFile))
                storePassword = uploadStorePassword
                keyAlias = uploadKeyAlias
                keyPassword = uploadKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasUploadSigning) {
                signingConfig = signingConfigs.getByName("releaseUpload")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.00")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}
