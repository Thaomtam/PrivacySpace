plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

// Helper function to read local.properties
fun getLocalProperty(key: String, defaultValue: String = ""): String {
    val properties = java.util.Properties()
    val localProperties = project.rootProject.file("local.properties")
    if (localProperties.exists()) {
        properties.load(localProperties.inputStream())
        return properties.getProperty(key, defaultValue)
    }
    return defaultValue ?: ""
}

android {
    compileSdk = 34 // Updated to support Android 14
    namespace = "cn.geektang.privacyspace"

    defaultConfig {
        applicationId = "cn.geektang.privacyspace"
        minSdk = 26
        targetSdk = 34 // Updated for Android 14 support
        versionCode = 23
        versionName = "1.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            // Try to get signing config from environment variables first (for CI)
            val envStoreFile = System.getenv("Space_storeFile")
            val envStorePassword = System.getenv("Space_storePassword")
            val envKeyAlias = System.getenv("Space_keyAlias")
            val envKeyPassword = System.getenv("Space_keyPassword")
            
            // If not in environment, try local.properties (for local builds)
            val localStoreFile = getLocalProperty("storeFile")
            val localStorePassword = getLocalProperty("storePassword")
            val localKeyAlias = getLocalProperty("keyAlias")
            val localKeyPassword = getLocalProperty("keyPassword")
            
            // Use env vars if available, otherwise use local properties
            storeFile = file(envStoreFile ?: localStoreFile)
            storePassword = envStorePassword ?: localStorePassword
            keyAlias = envKeyAlias ?: localKeyAlias
            keyPassword = envKeyPassword ?: localKeyPassword
            
            // Only enable signing if we have all required properties
            enableV1Signing = storeFile.exists()
            enableV2Signing = storeFile.exists()
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "APP_CENTER_SECRET", "\"${System.getenv("Space_AppCenterSecret") ?: ""}\"")
            signingConfig = if (signingConfigs.getByName("release").storeFile.exists()) {
                signingConfigs.getByName("release") 
            } else {
                signingConfigs.getByName("debug")
            }
        }
        release {
            buildConfigField("String", "APP_CENTER_SECRET", "\"${System.getenv("Space_AppCenterSecret") ?: ""}\"")
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (signingConfigs.getByName("release").storeFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
    }
    
    packaging {
        resources {
            excludes.add("/META-INF/**")
            excludes.add("okhttp3/**")
            excludes.add("kotlin/**")
            excludes.add("**.bin")
            excludes.add("**.properties")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.material:material:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["compose_version"]}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("com.google.accompanist:accompanist-insets:0.30.1")
    implementation("com.google.accompanist:accompanist-insets-ui:0.30.1")
    implementation("com.google.accompanist:accompanist-flowlayout:0.30.1")
    implementation("com.squareup.moshi:moshi:1.15.0")
    
    // Add org.lsposed.hiddenapibypass for hidden API access with updated version
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.1")
    
    // Use the modern EzXHelper library for Xposed hooks like HideMyAppList does
    implementation("com.github.kyuubiran:EzXHelper:2.0.8")
    
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    
    val appCenterSdkVersion = "5.0.0"
    implementation("com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${rootProject.extra["compose_version"]}")
    debugImplementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")
    
    compileOnly("de.robv.android.xposed:api:82")
    compileOnly("de.robv.android.xposed:api:82:sources")
    compileOnly(files("libs/android-30.jar"))
    compileOnly("androidx.recyclerview:recyclerview:1.2.1")
}