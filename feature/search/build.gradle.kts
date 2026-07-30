plugins { id("koalamusic.android.compose") }
android { namespace = "net.koalastuff.music.feature.search" }
dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
}
