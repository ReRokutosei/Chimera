import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.serialization") version "2.2.21"
    alias(libs.plugins.aboutLibraries)
}

android {
    namespace = "com.rerokutosei.chimera"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rerokutosei.chimera"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = project.findProperty("appVerName")?.toString() ?: "1.0.0"
    }
    
    // 配置ABI分包
    splits {
        abi {
            isEnable = false
            reset()
            include("arm64-v8a", "x86_64")
            isUniversalApk = true
        }
    }
    
    lint {
        disable += "MissingTranslation" // 禁用翻译资源文本检查
    }
    
    signingConfigs {
        create("release") {
            // 优先从环境变量读取，其次从本地gradle.properties读取
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: project.findProperty("KEYSTORE_PATH") as? String ?: "../keystore/Chimera.jks"
            storeFile = file(keystorePath)
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.findProperty("KEYSTORE_PASSWORD") as? String ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: project.findProperty("KEY_ALIAS") as? String ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: project.findProperty("KEY_PASSWORD") as? String ?: ""

            enableV1Signing = false
            enableV2Signing = false
            enableV3Signing = true
            enableV4Signing = true
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true          // 启用代码混淆和压缩
            isShrinkResources = true        // 启用资源压缩
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 仅在keystore文件存在时应用签名配置
            signingConfig = if (signingConfigs.getByName("release").storeFile?.exists() == true) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

// AboutLibraries配置
aboutLibraries {
    collect {
        // 启用平台依赖收集
        includePlatform = true
    }
    export {
        // 定义输出文件路径到res/raw目录
        outputFile = file("src/main/res/raw/aboutlibraries.json")
        prettyPrint = true
    }
}

// 注册生成AboutLibraries JSON文件的任务
tasks.register("generateAboutLibraries") {
    dependsOn("exportLibraryDefinitions")
}

// 注册执行Python脚本更新README文件的任务
tasks.register<Exec>("updateReadmeWithLicenses") {
    dependsOn("generateAboutLibraries")
    
    // 设置Python命令和脚本路径
    commandLine("python", "docs/update_readme.py")
    
    // 设置工作目录为项目根目录
    workingDir(project.rootDir)
    
    // 设置即使Python脚本执行失败也不影响构建过程
    isIgnoreExitValue = true
}

// 在构建前执行所有任务
tasks.named("preBuild") {
    dependsOn("updateReadmeWithLicenses")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    // 直接指定使用带 Expressive 功能的 Material 3 alpha 版本
    implementation("androidx.compose.material3:material3:1.5.0-alpha09")
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    // 添加DataStore数据存储库
    implementation(libs.androidx.datastore.preferences)
    // 添加Navigation导航库
    implementation(libs.androidx.navigation.compose)
    // 添加AppCompat支持
    implementation("androidx.appcompat:appcompat:1.7.1")

    implementation("androidx.compose.animation:animation:1.9.5")
    implementation("androidx.compose.animation:animation-core:1.9.5")

    // 添加Material3支持
    implementation("com.google.android.material:material:1.13.0")
    implementation(libs.androidx.material3)
    // 添加SubsamplingScaleImageView库用于大图显示
    implementation("com.davemorrissey.labs:subsampling-scale-image-view-androidx:3.10.0")
    
    // 添加ImageToolbox组件库
    implementation(project(":t8rin:fancy-slider-library"))
    implementation(project(":t8rin:embedded-picker-library"))
    implementation(project(":t8rin:image-reorder-carousel-library"))

    // Color picker compose library
    implementation("com.github.skydoves:colorpicker-compose:1.1.3")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation(libs.ui)
    
    // AboutLibraries 核心库
    implementation(libs.aboutlibraries.core)

    // 用于调试和Compose预览功能
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

detekt {
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
}