plugins { id("koalamusic.android.library") }
android { namespace = "net.koalastuff.music.core.testing" }
dependencies {
    api(projects.core.model)
    api(libs.junit)
    api(libs.mockwebserver)
    implementation(libs.kotlinx.coroutines.core)
}
