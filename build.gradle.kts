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
    id("org.jetbrains.intellij") version "1.11.0"
    id("com.github.gradle-git-version-calculator") version "1.0.0"
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
}

gitVersionCalculator {
    prefix = "v"
}

var channel : String = System.getenv("CHANNEL") ?: ""
group = "org.github.erikzielke.gotoproject"
version = gitVersionCalculator.calculateVersion()

repositories {
    mavenCentral()
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("223.7571.182")
    updateSinceUntilBuild.set(false)
}
tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
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

}
