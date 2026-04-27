// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.spotless)
}

allprojects {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        googleJavaFormat()
        target("**/*.java")
        targetExclude("**/AccessibilityInsightsForAndroidService.java", "**/AccessibilityNodeInfoSorter.java")
        licenseHeader("// Copyright (c) Microsoft Corporation.\n// Licensed under the MIT License.\n\n")
    }
    kotlin {
        ktlint()
        target("**/*.kt")
        licenseHeader("// Copyright (c) Microsoft Corporation.\n// Licensed under the MIT License.\n\n")
    }
}

tasks.register("fastpass") {
    dependsOn("spotlessCheck")
    dependsOn(":app:lint")
}

tasks.register("fastpassFix") {
    gradle.startParameter.isWriteDependencyLocks = true
    dependsOn("spotlessApply")
    dependsOn(":app:lintFix")
}

dependencyLocking {
    lockAllConfigurations()
}
