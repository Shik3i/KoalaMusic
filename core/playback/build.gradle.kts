plugins {
    id("koalamusic.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}
android { namespace = "net.koalastuff.music.core.playback" }
dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.opensubsonic)
    implementation(projects.core.security)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.okhttp)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
}
