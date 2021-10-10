plugins {
    kotlin("jvm") version "1.5.30" apply false
    id("com.diffplug.spotless") version "5.16.0"
}

buildscript {
    repositories {
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32")
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    spotless {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")
            ktlint("0.42.1")
            licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        }
    }
}
