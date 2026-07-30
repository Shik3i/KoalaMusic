plugins {
    id("koalamusic.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}
android { namespace = "net.koalastuff.music.core.security" }
dependencies {
    implementation(projects.core.common)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
}
