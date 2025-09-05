import java.util.Properties

plugins {
    java
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

group = "dev.sora"
version = "0.1.0"

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
