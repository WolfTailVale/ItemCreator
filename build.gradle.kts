import java.util.Properties

plugins {
    java
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

// Load version from properties file
val versionProps = Properties()
file("version.properties").inputStream().use { versionProps.load(it) }
val mcVersion = versionProps.getProperty("minecraft")
val pluginMajor = versionProps.getProperty("plugin_major")
val pluginMinor = versionProps.getProperty("plugin_minor")
val pluginPatch = versionProps.getProperty("plugin_patch")

group = "dev.sora"
version = "${pluginMajor}.${pluginMinor}.${pluginPatch}"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(
            "name" to project.name,
            "version" to project.version,
            "main" to "dev.sora.itemcreator.ItemCreatorPlugin"
        )
    }
}

// Create shaded jar if needed later
// tasks.jar { archiveClassifier.set("") }
