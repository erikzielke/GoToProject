plugins {
    id("org.jetbrains.intellij") version "1.2.1"
    kotlin("jvm") version "1.5.10"
}

group = "org.github.erikzielke.gotoproject"
version = "1.2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("202.6948.69")
}
tasks {
    patchPluginXml {
        changeNotes.set("""
            Converted to Kotlin<br>
            Projects available in Search Everywhere""".trimIndent())
    }
}
