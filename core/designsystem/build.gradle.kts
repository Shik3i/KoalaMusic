plugins { id("koalamusic.android.compose") }
android { namespace = "net.koalastuff.music.core.designsystem" }
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
