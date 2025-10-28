import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")

    // 필수
    id("kotlin-parcelize")
    id("kotlin-android")
    id("kotlin-kapt")

    // hilt 추가
    id("dagger.hilt.android.plugin")

    // Add the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")
}

// local.properties 파일 로드
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.hongslab.chating_memo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hongslab.chating_memo"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "1.5"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties에서 값 읽어오기
        buildConfigField("long", "KEY", localProperties.getProperty("KEY", "0"))
        buildConfigField("String", "CLOUD_NAME", "\"${localProperties.getProperty("CLOUD_NAME", "")}\"")
        buildConfigField("String", "UPLOAD_PRESET", "\"${localProperties.getProperty("UPLOAD_PRESET", "")}\"")
        buildConfigField("String", "API_KEY", "\"${localProperties.getProperty("API_KEY", "")}\"")
        buildConfigField("String", "API_SECRET", "\"${localProperties.getProperty("API_SECRET", "")}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("../key")
            storePassword = "111111"
            keyAlias = "key0"
            keyPassword = "111111"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }

    hilt {
        enableAggregatingTask = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")



    //dex
    implementation("androidx.multidex:multidex:2.0.1")

    // viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    //Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    kapt("com.google.dagger:hilt-compiler:2.57.2")

    //jsoup
    implementation("org.jsoup:jsoup:1.21.2")

    //이미지 캐싱 로드
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    // recyclerView flex
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // 이미지 업로드
    implementation("com.cloudinary:cloudinary-android:3.1.2")
    implementation("com.squareup.okhttp3:okhttp:5.2.1")
    implementation("com.squareup.okhttp3:logging-interceptor:5.2.1") // 로깅용 (선택사항)

    // 이미지 확대 축소
    implementation("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // TedPermission Coroutine
    implementation("io.github.ParkSangGwon:tedpermission-coroutine:3.4.2")

    // grid layout
    implementation("androidx.gridlayout:gridlayout:1.1.0")

    //ads
    implementation("com.google.android.gms:play-services-ads:24.7.0")
}