// Root-Build-Skript: definiert nur die verwendeten Plugin-Versionen,
// angewandt werden sie im jeweiligen Modul (app)
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // Compose-Compiler ist seit Kotlin 2.0 ein eigenes Plugin
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    // KSP für die Room-Annotation-Verarbeitung (Version an Kotlin 2.0.21 gekoppelt)
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}
