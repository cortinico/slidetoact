plugins {
    id("com.android.application")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "com.ncorti.slidetoact.example"
        minSdk = 14
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    lint {
        abortOnError = true
    }
    namespace = "com.ncorti.slidetoact.example"
}

dependencies {
    implementation(project(":slidetoact"))

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
