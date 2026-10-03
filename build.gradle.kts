import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel
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
    id("org.jetbrains.intellij.platform") version "2.19.0"
    id("com.github.gradle-git-version-calculator") version "1.1.0"
    id("org.jetbrains.kotlin.jvm") version "2.4.20"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.kotlinx.kover") version "0.9.9"
    id("com.diffplug.spotless") version "8.10.2"
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
        intellijIdea(providers.gradleProperty("platformVersion"))
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        pluginVerifier()
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.4.0")
}

// Oldest supported IntelliJ build. Pinned so that building against a newer platformVersion doesn't raise it.
// No until-build: the plugin stays installable on new IDE releases, which verifyPlugin checks weekly in CI.
val supportedSinceBuild = "253"

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = supportedSinceBuild
        }
    }
    pluginVerification {
        ides {
            // Every released IntelliJ IDEA from the oldest supported build onwards, newest patch of each major version.
            select {
                types = listOf(IntelliJPlatformType.IntellijIdea)
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = supportedSinceBuild
            }
        }
        failureLevel = listOf(FailureLevel.COMPATIBILITY_PROBLEMS, FailureLevel.INVALID_PLUGIN)
    }
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
