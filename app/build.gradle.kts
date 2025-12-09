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

    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.fragment:fragment:1.6.1")

    // 添加网络请求依赖
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    // 确保有以下网络依赖
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // 添加HTML解析依赖
    implementation("org.jsoup:jsoup:1.16.1")

    // 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // 添加Room数据库
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    annotationProcessor("androidx.room:room-compiler:2.5.2")

    // 添加微信SDK依赖
    implementation("com.tencent.mm.opensdk:wechat-sdk-android:6.8.0")

    implementation("com.aliyun.dpa:oss-android-sdk:2.9.16")

    // 文件选择和权限
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("com.guolindev.permissionx:permissionx:1.7.1")

    // 图片加载和显示（用于文件预览）
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // 阿里云OSS SDK
    implementation("com.aliyun.dpa:oss-android-sdk:2.9.16")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}