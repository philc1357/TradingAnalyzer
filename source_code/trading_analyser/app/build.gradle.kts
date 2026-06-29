// App-Modul: Android-Konfiguration und Build-Einstellungen
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.tradinganalyser"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tradinganalyser"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Standard-App-Name (wird je Modell-Flavor überschrieben)
        manifestPlaceholders["appLabel"] = "Trading Analyzer"
    }

    // ============================================================
    // Modell-Flavors: pro NVIDIA-Modell eine eigene APK. Jeder Flavor
    // spritzt seine KI-Parameter über BuildConfig ein (siehe
    // network/AnalyzerService.kt) und bekommt einen eigenen
    // applicationId-Suffix, damit beide Apps parallel installierbar sind.
    // ============================================================
    flavorDimensions += "model"
    productFlavors {
        create("nanoVl") {
            dimension = "model"
            applicationIdSuffix = ".nanovl"
            versionNameSuffix = "-nano-vl"
            manifestPlaceholders["appLabel"] = "Trading Analyzer (Nano VL)"
            buildConfigField("String", "AI_MODEL", "\"nvidia/nemotron-nano-12b-v2-vl\"")
            buildConfigField("float", "AI_TEMPERATURE", "0.2f")
            buildConfigField("float", "AI_TOP_P", "0.95f")
            buildConfigField("int", "AI_MAX_TOKENS", "1024")
            buildConfigField("boolean", "AI_REASONING", "false")
            buildConfigField("int", "AI_REASONING_BUDGET", "0")
        }
        create("omniReasoning") {
            dimension = "model"
            applicationIdSuffix = ".omnireasoning"
            versionNameSuffix = "-omni-reasoning"
            manifestPlaceholders["appLabel"] = "Trading Analyzer (Omni Reasoning)"
            buildConfigField("String", "AI_MODEL", "\"nvidia/nemotron-3-nano-omni-30b-a3b-reasoning\"")
            buildConfigField("float", "AI_TEMPERATURE", "0.6f")
            buildConfigField("float", "AI_TOP_P", "0.95f")
            buildConfigField("int", "AI_MAX_TOKENS", "8192")
            buildConfigField("boolean", "AI_REASONING", "true")
            buildConfigField("int", "AI_REASONING_BUDGET", "4096")
        }
    }

    // APK-Dateiname trägt den Flavor-(Modell-)Namen, z. B.
    // trading-analyser-nanoVl-debug.apk
    applicationVariants.all {
        val variant = this
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl)
                .outputFileName = "trading-analyser-${variant.flavorName}-${variant.buildType.name}.apk"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    implementation("androidx.core:core-ktx:1.13.1")

    // Jetpack Compose (Versionen zentral über die BOM)
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Room (lokale SQLite-Datenbank) mit KSP-Codegenerierung
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Netzwerk: direkter NVIDIA-API-Aufruf
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Bildanzeige (Vorschau & Liste)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Verschlüsselte Ablage des API-Keys
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Hinweis: Das kumulierte PnL-Diagramm wird ohne externe Bibliothek
    // direkt mit Compose-Canvas gezeichnet (siehe ui/components/PnlChart.kt).
}
