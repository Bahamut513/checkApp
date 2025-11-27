plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.check"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.check"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 添加微信SDK的manifest占位符
        manifestPlaceholders["WX_APP_ID"] = "wx13439bc546007458" // 替换为你的AppID
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 添加网络请求依赖
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // 添加HTML解析依赖
    implementation("org.jsoup:jsoup:1.16.1")

    // 添加Room数据库
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    annotationProcessor("androidx.room:room-compiler:2.5.2")

    // 添加微信SDK依赖
    implementation("com.tencent.mm.opensdk:wechat-sdk-android:6.8.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}