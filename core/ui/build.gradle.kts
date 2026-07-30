plugins { id("koalamusic.android.compose") }
android { namespace = "net.koalastuff.music.core.ui" }
dependencies {
    api(projects.core.model)
    implementation(projects.core.designsystem)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
