plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("signing")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":library"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.30-1.0.0")
    implementation("com.squareup:kotlinpoet-ksp:1.10.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.5")
}

group = project.extra["groupId"] as String
version = project.extra["versionName"] as String

val sonatypeUsername = if (rootProject.hasProperty("sonatypeUsername")) {
    rootProject.property("sonatypeUsername") as String
} else {
    ""
}
val sonatypePassword = if (rootProject.hasProperty("sonatypePassword")) {
    rootProject.property("sonatypePassword") as String
} else {
    ""
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("javadoc"))
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(buildDir.resolve("javadoc"))
}

group = project.extra["groupId"] as String
version = project.extra["versionName"] as String

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = project.extra["processorArtifactId"] as String
                from(components["java"])
                artifact(javadocJar)
                pom {
                    name.set("Compose NavGen Processor")
                    description.set(project.extra["pomDescription"] as String)
                    url.set(project.extra["pomUrl"] as String)
                    licenses {
                        license {
                            name.set(project.extra["pomLicense"] as String)
                            url.set(project.extra["pomLicenseUrl"] as String)
                        }
                    }
                    developers {
                        developer {
                            id.set(project.extra["pomDeveloperId"] as String)
                            name.set(project.extra["pomDeveloperName"] as String)
                            email.set(project.extra["pomDeveloperEmail"] as String)
                        }
                    }
                    scm {
                        connection.set(project.extra["pomScmUrl"] as String)
                        developerConnection.set(project.extra["pomScmUrl"] as String)
                        url.set(project.extra["pomUrl"] as String)
                    }
                }
            }

            repositories {
                maven {
                    val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                    val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
                    url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                    credentials {
                        username = sonatypeUsername
                        password = sonatypePassword
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
