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
    id("org.jetbrains.intellij") version "1.2.1"
    id("com.github.gradle-git-version-calculator") version "1.0.0"
    kotlin("jvm") version "1.5.10"
}

gitVersionCalculator {
    prefix = "v"
}

var channel : String = System.getenv("CHANNEL") ?: "alpha"
group = "org.github.erikzielke.gotoproject"
version = gitVersionCalculator.calculateVersion()

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("211.6693.111")
    updateSinceUntilBuild.set(false)
}
tasks {
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
