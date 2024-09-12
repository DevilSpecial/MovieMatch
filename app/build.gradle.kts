plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.moviematch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.moviematch"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding  = true
    }
}

dependencies {
    implementation ("com.github.Yalantis:Koloda-Android:v0.0.2-alpha")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.material)
    implementation(platform(libs.firebase.bom))
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    implementation (libs.retrofit)
    implementation ("com.github.yuyakaido:CardStackView:v2.3.4")
    implementation (libs.converter.gson)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.play.services.auth)
    implementation ("com.google.firebase:firebase-database")

    implementation (libs.adapter.rxjava2)
    implementation (libs.androidx.recyclerview)
    implementation (libs.coil)


}