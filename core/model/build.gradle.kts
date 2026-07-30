plugins { id("koalamusic.android.library") }
android { namespace = "net.koalastuff.music.core.model" }
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
