plugins {
    id("koalamusic.android.compose")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}
android { namespace = "net.koalastuff.music.feature.library" }
dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.playback)
    implementation(projects.core.ui)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.paging.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
