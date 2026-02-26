import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("gradle.plugin.com.github.gradle-git-version-calculator:gradle-git-version-calculator:1.1.0")
    }
}
plugins {
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("com.github.gradle-git-version-calculator") version "1.1.0"
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.kotlinx.kover") version "0.9.7"
    id("com.diffplug.spotless") version "8.2.1"
}

gitVersionCalculator {
    prefix = "v"
}

var channel: String = System.getenv("CHANNEL") ?: ""
group = "org.github.erikzielke.gotoproject"
version = gitVersionCalculator.calculateVersion()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.3")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.2.3")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }
    patchPluginXml {
        changeNotes.set(
            """
            Thanks to https://github.com/ChrisCarini :
            <ul>
                <li>Added option for showing 'Projects' tab in search everywhere and opening it by default when using 'Go to Project' action.</li>
                <li>Disabling Go to Last Project action if no project available.</li>
            </ul>
            """.trimIndent(),
        )
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
        channels.set(listOf(channel))
    }

    // Custom task to run detekt and generate reports
    register<io.gitlab.arturbosch.detekt.Detekt>("detektAll") {
        description = "Run detekt analysis on the whole project"
        parallel = true
        buildUponDefaultConfig = true
        setSource(files(projectDir))
        config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
        include("**/*.kt", "**/*.kts")
        exclude("**/build/**", "**/resources/**")
        reports {
            sarif.required = true
            sarif.outputLocation.set(file("$projectDir/build/reports/detekt/detekt.sarif"))
            html.required = true
        }
    }
}

kover {
    reports {
        verify {
            // add new verification rule
            rule {
                minBound(30)
            }
        }
    }
}

spotless {
    kotlin {
        // Use ktlint for Kotlin formatting
        ktlint()
        // Enforce specific end-of-line character
        endWithNewline()
        // Remove trailing whitespace
        trimTrailingWhitespace()
        // Enforce specific indentation
        leadingTabsToSpaces(4)
        // Apply formatting to all Kotlin files
        target("src/**/*.kt")
    }
    kotlinGradle {
        // Use ktlint for Kotlin Gradle files formatting
        ktlint()
        // Enforce specific end-of-line character
        endWithNewline()
        // Remove trailing whitespace
        trimTrailingWhitespace()
        // Enforce specific indentation
        leadingTabsToSpaces(4)
        // Apply formatting to all Kotlin Gradle files
        target("*.gradle.kts")
    }
}
