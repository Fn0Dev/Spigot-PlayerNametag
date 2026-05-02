import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.20"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

val target_java = 21
group = "org.fnzero"
version = "1.0.0-R0"

repositories {
    mavenCentral()
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // PaperAPI
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")

    // PlacholderAPI
    compileOnly("me.clip:placeholderapi:2.11.7")

    // LuckPerms
    compileOnly("net.luckperms:api:5.5")

    // StandardAPI
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}

tasks {
    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")

        manifest {
            attributes["paperweight-mappings-namespace"] = "spigot"
        }

        relocate("org.jetbrains.kotlin", "org.fnzero.mmo.libs.kotlin")
    }

    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

kotlin {
    jvmToolchain(target_java)
}
