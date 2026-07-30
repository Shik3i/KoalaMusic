plugins { id("koalamusic.android.library") }
android { namespace = "net.koalastuff.music.core.opensubsonic" }
dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
}
