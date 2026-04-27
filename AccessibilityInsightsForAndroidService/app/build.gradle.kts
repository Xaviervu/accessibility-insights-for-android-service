// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.microsoft.accessibilityinsightsforandroidservice"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.microsoft.accessibilityinsightsforandroidservice"
        minSdk = 24
        targetSdk = 36
        versionCode = if (project.hasProperty("apkVersionCode")) project.property("apkVersionCode").toString().toInt() else 1
        versionName = project.findProperty("apkVersionName")?.toString() ?: "DEVELOPMENT"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isDebuggable = false
        }

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        buildConfig = true
    }
    lint {
        abortOnError = true
        checkAllWarnings = true
        disable += listOf("ConvertToWebp", "GoogleAppIndexingWarning", "SyntheticAccessor", "UnsafeExperimentalUsageError", "UnsafeExperimentalUsageWarning")
        informational += listOf("GradleDependency", "NewerVersionAvailable", "OldTargetApi", "UnknownNullness")
        warningsAsErrors = true
    }
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

dependencies {
    // Non-dev dependencies (redistributed with releases)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.axe.android)
    implementation(libs.accessibility.test.framework) {
        exclude(group = "org.checkerframework", module = "checker")
    }
    implementation(libs.gson)
    implementation(libs.guava)

    // Dev dependencies (not redistributed)
    androidTestImplementation(libs.espresso.core)
    lintChecks(libs.commons.compress)
    lintChecks(libs.bouncycastle.bcpkix)
    lintChecks(libs.bouncycastle.bcprov)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.inline)
}

configurations.configureEach {
    resolutionStrategy {
        force("commons-codec:commons-codec:1.15")
        force("junit:junit:4.13.2")
        force("org.jsoup:jsoup:1.15.4")
        force("com.google.protobuf:protobuf-java:3.22.0")
    }
}

dependencyLocking {
    lockAllConfigurations()
}
