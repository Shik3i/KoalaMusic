plugins {
    id("koalamusic.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}
android { namespace = "net.koalastuff.music.core.data" }
dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.opensubsonic)
    implementation(projects.core.security)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
}
