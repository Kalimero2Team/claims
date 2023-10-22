import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.paper.run)
    alias(libs.plugins.plugin.yml)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
}

dependencies {
    compileOnly(libs.paper.api)
    bukkitLibrary(libs.cloud.paper)
    implementation(project(":claims-api"))
}

tasks{
    assemble {
        dependsOn(shadowJar)
    }
    runServer{
        minecraftVersion("1.20.1")
    }
}

bukkit {
    main = "com.kalimero2.team.claims.paper.PaperClaims"
    apiVersion = "1.20"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    authors = listOf("byquanton")
    softDepend = listOf("floodgate")
}