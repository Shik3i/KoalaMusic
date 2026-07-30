plugins {
    id("koalamusic.android.library")
    alias(libs.plugins.ksp)
}
android { namespace = "net.koalastuff.music.core.database" }
dependencies {
    api(projects.core.model)
    api(libs.androidx.room.runtime)
    implementation(projects.core.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
}
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
