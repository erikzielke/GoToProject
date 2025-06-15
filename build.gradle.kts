import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("gradle.plugin.com.github.gradle-git-version-calculator:gradle-git-version-calculator:1.0.0")
    }
}
plugins {
    id("org.jetbrains.intellij.platform") version "2.6.0"
    id("com.github.gradle-git-version-calculator") version "1.0.0"
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
}

gitVersionCalculator {
    prefix = "v"
}

var channel : String = System.getenv("CHANNEL") ?: ""
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
        intellijIdeaCommunity("2025.1.1.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
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
        changeNotes.set("""
            Converted to Kotlin<br>
            Projects available in Search Everywhere""".trimIndent())
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
            sarif.outputLocation.set(file("${projectDir}/build/reports/detekt/detekt.sarif"))
            html.required = true
        }
    }

}

koverReport {
    // Configure default report
    defaults {
        // Enable HTML and XML reports
        html {
            title = "GoToProject - Kover Coverage Report"
            onCheck = true
        }
        xml {
            onCheck = true
        }
    }

    // Configure filters if needed
    filters {
        // Exclude test classes from coverage
        excludes {
            // classes("*Test*")
        }
    }

    // Set verification rules
    verify {
        // Set minimum line coverage threshold to 70%
        rule {
            isEnabled = true
            entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION
            bound {
                minValue = 10
                aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
            }
        }
    }
}
