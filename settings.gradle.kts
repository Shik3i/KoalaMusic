pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KoalaMusic"

include(
    ":app",
    ":core:common",
    ":core:model",
    ":core:opensubsonic",
    ":core:database",
    ":core:data",
    ":core:security",
    ":core:playback",
    ":core:designsystem",
    ":core:ui",
    ":core:testing",
    ":feature:setup",
    ":feature:home",
    ":feature:library",
    ":feature:search",
    ":feature:player",
    ":feature:settings",
)
