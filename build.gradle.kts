import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

group = "org.freedomcraft"

val mcVersion = project.extra["mcVersion"]

val serverPath = project.extra["serverPath"]

val versionPropertiesFile = File("version.properties")
val versionProperties = if (versionPropertiesFile.exists()) Properties().apply {
    FileInputStream(versionPropertiesFile).use { stream ->
        load(stream)
    }
} else Properties()

val githubPropertiesFile = File("${project.projectDir}/${project.extra["gitPropertiesPath"]}")
val githubProperties = if (githubPropertiesFile.exists()) Properties().apply {
    FileInputStream(githubPropertiesFile).use { stream ->
        load(stream)
    }
} else null

val rawBuildNumber = versionProperties["buildNumber"]
var buildNumber = if (rawBuildNumber is Int) rawBuildNumber else rawBuildNumber?.toString()?.toInt() ?: 0
val newBuildNumber = if (System.getenv("NO_INCREMENT_VERSION") != null) buildNumber else buildNumber + 1

val projectName = if (project.extra.has("name")) project.extra["name"].toString() else project.name

description = projectName
val artifact = projectName.lowercase()

plugins {
    java
    eclipse
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("io.papermc.paperweight.userdev") version "1.5.9"
    id("xyz.jpenilla.run-paper") version "1.0.4" // Adds runServer and runMojangMappedServer tasks for testing
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()

    maven {
        url = uri("https://maven.pkg.github.com/FreedomCraft-Network/maven-repo/")

        credentials {
            username = githubProperties?.getProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
            password = githubProperties?.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/groups/public/")
    }

    maven {
        url = uri("https://nexus.velocitypowered.com/repository/maven-public/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://repo.essentialsx.net/releases/")
    }

    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }

    maven {
        url = uri("https://jitpack.io/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        url = uri("https://repo.loohpjames.com/repository/")
    }

    //PlaceholderAPI
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    //WorldGuard
    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }

    //MythicMobs
    maven {
        url = uri("https://mvn.lumine.io/repository/maven-public/")
    }
}

dependencies {
    paperweight.paperDevBundle("${mcVersion}-R0.1-SNAPSHOT")

    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("me.clip:placeholderapi:2.11.1")

    //WorldEdit API
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.0.0") {
        exclude("org.bukkit", "bukkit")
    }
    //WorldGuard API
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.0") {
        exclude("org.bukkit", "bukkit")
    }

    compileOnly("io.lumine:Mythic-Dist:5.3.5")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    // Run reobfJar on build
    build {
        dependsOn("reobfJar")

        doLast {
            delete("Release/${projectName}-${mcVersion}-${buildNumber}.jar")
            copy {
                from(project.tasks.reobfJar.get().outputJar)
                into("Release/")
                rename { _ -> "${projectName}-${mcVersion}-${newBuildNumber}.jar" }
            }

            if (newBuildNumber != buildNumber) {
                versionProperties["buildNumber"] = newBuildNumber.toString()
                FileOutputStream(versionPropertiesFile).use { stream ->
                    versionProperties.store(stream, null)
                }
            }
        }
    }

  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.compilerArgs.plusAssign("-Xlint:deprecation")
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }
  processResources {
    filteringCharset = Charsets.UTF_8.name()
  }
  shadowJar {
    archiveFileName.set("${projectName}.jar")
  	minimize()
  }
}

task("exportRelease") {
    dependsOn("build")

    doLast {
        delete("${serverPath}plugins/${projectName}-${mcVersion}-${buildNumber}.jar")
        copy {
            from("Release/${projectName}-${mcVersion}-${newBuildNumber}.jar")
            into("${serverPath}plugins/")
            rename { _ -> "${projectName}-${mcVersion}.jar" }
        }
    }
}

val shadowJarArtifact = artifacts.add("archives", file("build/libs/${projectName}.jar")) {
    type = "shadowJar"
    builtBy("shadowJar")
}

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/FreedomCraft-Network/maven-repo/")

            credentials {
                username = githubProperties?.getProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
                password = githubProperties?.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications.create<MavenPublication>("maven") {
        artifactId = artifact

        artifact(shadowJarArtifact)
    }
}