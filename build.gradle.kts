plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")

    tasks.withType<Test>().configureEach {
        // Gradle 9.4 sees Hilt/KSP generated test source directories as authored
        // tests. Source-empty Android modules are valid during foundation setup.
        failOnNoDiscoveredTests = false
    }
}
