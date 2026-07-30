plugins {
    `kotlin-dsl`
}

group = "net.koalastuff.music.buildlogic"

dependencies {
    implementation("com.android.tools.build:gradle:9.3.1")
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.4.10")
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "koalamusic.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "koalamusic.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
    }
}
