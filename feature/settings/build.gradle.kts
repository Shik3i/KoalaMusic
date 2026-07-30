plugins { id("koalamusic.android.compose") }
android { namespace = "net.koalastuff.music.feature.settings" }
dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
}
